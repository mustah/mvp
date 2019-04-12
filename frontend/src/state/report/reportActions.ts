import {find, first, take, uniqBy} from 'lodash';
import {createAction, createStandardAction} from 'typesafe-actions';
import {TemporalResolution} from '../../components/dates/dateModels';
import {Maybe} from '../../helpers/Maybe';
import {GetState} from '../../reducers/rootReducer';
import {firstUpperTranslated} from '../../services/translationService';
import {Dispatcher, uuid} from '../../types/Types';
import {allQuantitiesMap, Medium, Quantity} from '../ui/graph/measurement/measurementModels';
import {showFailMessage} from '../ui/message/messageActions';
import {SelectionInterval} from '../user-selection/userSelectionModels';
import {
  LegendItem,
  LegendItemSettings,
  LegendType,
  QuantityId,
  QuantityLegendType,
  ReportSector,
  SavedReportsState
} from './reportModels';
import {getLegendItems, getSelectedQuantitiesMap, hasSelectedQuantities} from './reportSelectors';

export const addLegendItems = (sector: ReportSector) =>
  createStandardAction(`DOMAIN_MODELS_REQUEST_${sector}`)<LegendItem[]>();

export const selectResolution = (sector: ReportSector) =>
  createStandardAction(`SELECT_RESOLUTION_${sector}`)<TemporalResolution>();

export const toggleComparePeriod = (sector: ReportSector) =>
  createAction(`TOGGLE_COMPARE_PERIOD_${sector}`);

export const toggleShowAverage = (sector: ReportSector) =>
  createAction(`TOGGLE_SHOW_AVERAGE_${sector}`);

export const toggleLine = (sector: ReportSector) =>
  createStandardAction(`TOGGLE_LINE_${sector}`)<uuid>();

export const toggleQuantityByType = (sector: ReportSector) =>
  createStandardAction(`TOGGLE_QUANTITY_BY_TYPE_${sector}`)<QuantityLegendType>();

export const toggleQuantityById = (sector: ReportSector) =>
  createStandardAction(`TOGGLE_QUANTITY_BY_ID_${sector}`)<QuantityId>();

export const showHideAllByType = (sector: ReportSector) =>
  createStandardAction(`SHOW_HIDE_ALL_BY_TYPE_${sector}`)<LegendType>();

export const showHideLegendRows = (sector: ReportSector) =>
  createStandardAction(`SHOW_HIDE_LEGEND_ROWS_${sector}`)<LegendType>();

export const removeAllByType = (sector: ReportSector) =>
  createStandardAction(`REMOVE_ALL_BY_TYPE_${sector}`)<LegendType>();

export const setReportTimePeriod = (sector: ReportSector) =>
  createStandardAction(`SET_REPORT_TIME_PERIOD_${sector}`)<SelectionInterval>();

export const limit: number = 100;

interface DispatchWithinLimits {
  dispatch: Dispatcher;
  items: LegendItem[];
  reportSection: ReportSector;
}

interface LegendTyped {
  type: LegendType;
}

export const getDefaultQuantity = <T extends LegendTyped>({type}: T): Quantity => allQuantitiesMap[type][0];

const findByType = (legendItems: LegendItem[], {type}: LegendTyped): Maybe<LegendItem> =>
  Maybe.maybe<LegendItem>(find(legendItems, {type}));

const pickJustSettings = ({isHidden, isRowExpanded}: LegendItem): LegendItemSettings =>
  ({isHidden, isRowExpanded: isRowExpanded || false});

const selectItemsIfWithinLimits = ({dispatch, items, reportSection}: DispatchWithinLimits) => {
  if (items.length > limit) {
    dispatch(showFailMessage(firstUpperTranslated(
      'only {{limit}} meters can be selected at the same time', {limit},
    )));
    return;
  }
  dispatch(addLegendItems(reportSection)(uniqBy(items, 'id')));
};

export const addToReport = (legendItem: LegendItem) =>
  (dispatch, getState: GetState) => {
    const savedReports = getState().report.savedReports;
    const legendItems = getLegendItems(savedReports);
    const {type, id} = legendItem;
    if (type !== Medium.unknown && !find(legendItems, {id})) {
      const selectedQuantities = getSelectedQuantitiesMap(savedReports)[legendItem.type];
      const quantities = hasSelectedQuantities(savedReports)
        ? selectedQuantities
        : selectedQuantities.length ? selectedQuantities : [getDefaultQuantity(legendItem)];
      const item = findByType(legendItems, legendItem)
        .map(it => ({...legendItem, ...pickJustSettings(it), quantities}))
        .orElse({...legendItem, quantities});
      selectItemsIfWithinLimits({dispatch, items: [...legendItems, item], reportSection: ReportSector.report});
    }
  };

export const deleteItem = (id: uuid) =>
  (dispatch, getState: GetState) => {
    const legendItems = getLegendItems(getState().report.savedReports);
    const items = legendItems.filter(it => it.id !== id);
    if (legendItems.length !== items.length) {
      selectItemsIfWithinLimits({dispatch, items, reportSection: ReportSector.report});
    }
  };

export const addAllToSelectionReport = (newLegendItems: LegendItem[]) =>
  (dispatch, getState: GetState) => {
    addAllToReportSector(
      dispatch,
      newLegendItems,
      getState().selectionReport.savedReports,
      ReportSector.selectionReport
    );
  };

export const addAllToReport = (newLegendItems: LegendItem[]) =>
  (dispatch, getState: GetState) => {
    addAllToReportSector(
      dispatch,
      newLegendItems,
      getState().report.savedReports,
      ReportSector.report
    );
  };

export const addAllToReportSector = (
  dispatch,
  newLegendItems: LegendItem[],
  savedReports: SavedReportsState,
  reportSection: ReportSector
) => {

  const legendItems = newLegendItems.filter(it => it.type !== Medium.unknown);
  const quantities = hasSelectedQuantities(savedReports) ? [] : take(legendItems, 1).map(getDefaultQuantity);
  const quantity = first(quantities);

  const savedLegendItems = getLegendItems(savedReports);
  const items = [
    ...savedLegendItems,
    ...legendItems
      .map(item => getDefaultQuantity(item) === quantity ? ({...item, quantities}) : item)
      .map(item => findByType(savedLegendItems, item)
        .map(it => ({...item, ...pickJustSettings(it)}))
        .orElse(item))
  ];
  selectItemsIfWithinLimits({dispatch, items, reportSection});
};

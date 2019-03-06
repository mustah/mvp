import {find, first, take, uniqBy} from 'lodash';
import {createAction, createStandardAction} from 'typesafe-actions';
import {TemporalResolution} from '../../components/dates/dateModels';
import {Maybe} from '../../helpers/Maybe';
import {GetState} from '../../reducers/rootReducer';
import {firstUpperTranslated} from '../../services/translationService';
import {allQuantitiesMap, Medium, Quantity} from '../../state/ui/graph/measurement/measurementModels';
import {showFailMessage} from '../../state/ui/message/messageActions';
import {SelectionInterval} from '../../state/user-selection/userSelectionModels';
import {Dispatcher, uuid} from '../../types/Types';
import {LegendItem, LegendType, QuantityId, QuantityLegendType} from './reportModels';
import {getLegendItems, hasSelectedQuantities} from './reportSelectors';

export const addLegendItems = createStandardAction('ADD_LEGEND_ITEMS')<LegendItem[]>();
export const selectResolution = createStandardAction('SELECT_RESOLUTION')<TemporalResolution>();
export const toggleComparePeriod = createAction('TOGGLE_COMPARE_PERIOD');
export const toggleLine = createStandardAction('TOGGLE_LINE')<uuid>();
export const toggleQuantityByType = createStandardAction('TOGGLE_QUANTITY_BY_TYPE')<QuantityLegendType>();
export const toggleQuantityById = createStandardAction('TOGGLE_QUANTITY_BY_ID')<QuantityId>();
export const showHideAllByType = createStandardAction('SHOW_HIDE_ALL_BY_TYPE')<LegendType>();
export const showHideLegendRows = createStandardAction('SHOW_HIDE_LEGEND_ROWS')<LegendType>();
export const removeAllByType = createStandardAction('REMOVE_ALL_BY_TYPE')<LegendType>();
export const setReportTimePeriod = createStandardAction('SET_REPORT_TIME_PERIOD')<SelectionInterval>();

export const limit: number = 100;

interface DispatchWithinLimits {
  dispatch: Dispatcher;
  items: LegendItem[];
}

interface LegendTyped {
  type: LegendType;
}

export const getDefaultQuantity = <T extends LegendTyped>({type}: T): Quantity => allQuantitiesMap[type][0];

const findByType = (legendItems: LegendItem[], {type}: LegendTyped): Maybe<LegendItem> =>
  Maybe.maybe<LegendItem>(find(legendItems, {type}));

const copyPropertiesFrom = (savedLegendItems, item): LegendItem =>
  findByType(savedLegendItems, item)
    .map(it => ({...it, ...item}))
    .orElse(item);

const selectItemsIfWithinLimits = ({dispatch, items}: DispatchWithinLimits) => {
  if (items.length > limit) {
    dispatch(showFailMessage(firstUpperTranslated(
      'only {{limit}} meters can be selected at the same time', {limit},
    )));
    return;
  }
  dispatch(addLegendItems(uniqBy(items, 'id')));
};

export const addToReport = (legendItem: LegendItem) =>
  (dispatch, getState: GetState) => {
    const savedReports = getState().report.savedReports;
    const legendItems = getLegendItems(savedReports);
    const {type, id} = legendItem;
    if (type !== Medium.unknown && !find(legendItems, {id})) {
      const quantities = hasSelectedQuantities(savedReports) ? [] : [getDefaultQuantity(legendItem)];
      const item = copyPropertiesFrom(legendItems, {...legendItem, quantities});
      selectItemsIfWithinLimits({dispatch, items: [...legendItems, item]});
    }
  };

export const deleteItem = (id: uuid) =>
  (dispatch, getState: GetState) => {
    const legendItems = getLegendItems(getState().report.savedReports);
    const items = legendItems.filter(it => it.id !== id);
    if (legendItems.length !== items.length) {
      selectItemsIfWithinLimits({dispatch, items});
    }
  };

export const addAllToReport = (newLegendItems: LegendItem[]) =>
  (dispatch, getState: GetState) => {
    const savedReports = getState().report.savedReports;
    const legendItems = newLegendItems.filter(it => it.type !== Medium.unknown);
    const quantities = hasSelectedQuantities(savedReports) ? [] : take(legendItems, 1).map(getDefaultQuantity);
    const quantity = first(quantities);

    const savedLegendItems = getLegendItems(savedReports);
    const items = [
      ...savedLegendItems,
      ...legendItems
        .map(item => getDefaultQuantity(item) === quantity ? ({...item, quantities}) : item)
        .map(item => copyPropertiesFrom(savedLegendItems, item))
    ];
    selectItemsIfWithinLimits({dispatch, items});
  };

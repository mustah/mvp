import {find, uniqBy} from 'lodash';
import {flatMap} from 'tslint/lib/utils';
import {createAction, createStandardAction} from 'typesafe-actions';
import {TemporalResolution} from '../../components/dates/dateModels';
import {unique} from '../../helpers/collections';
import {Maybe} from '../../helpers/Maybe';
import {GetState} from '../../reducers/rootReducer';
import {firstUpperTranslated} from '../../services/translationService';
import {allQuantitiesMap, Medium} from '../../state/ui/graph/measurement/measurementModels';
import {showFailMessage} from '../../state/ui/message/messageActions';
import {SelectionInterval} from '../../state/user-selection/userSelectionModels';
import {Dispatcher, uuid} from '../../types/Types';
import {LegendItem, LegendType, QuantityId, QuantityLegendType} from './reportModels';
import {getLegendItems} from './reportSelectors';

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
    const {report: {savedReports}} = getState();
    const legendItems: LegendItem[] = getLegendItems(savedReports);
    const {type, id} = legendItem;
    if (type !== Medium.unknown && find(legendItems, it => it.id === id) === undefined) {
      const item: LegendItem = Maybe.maybe<LegendItem>(find(legendItems, {type: legendItem.type}))
        .map(it => ({...it, ...legendItem}))
        .orElse(legendItem);
      const numSelectedQuantities = unique(flatMap(legendItems, it => it.quantities)).length;
      const quantities = numSelectedQuantities < 2 ? [allQuantitiesMap[item.type][0]] : [];
      const items: LegendItem[] = [...legendItems, {...item, quantities}];
      selectItemsIfWithinLimits({dispatch, items});
    }
  };

export const deleteItem = (id: uuid) =>
  (dispatch, getState: GetState) => {
    const legendItems = getLegendItems(getState().report.savedReports);
    const items: LegendItem[] = legendItems.filter((item: LegendItem) => item.id !== id);
    if (legendItems.length !== items.length) {
      selectItemsIfWithinLimits({dispatch, items});
    }
  };

export const addAllToReport = (items: LegendItem[]) =>
  (dispatch, getState: GetState) => {
    const legendItems: LegendItem[] = getLegendItems(getState().report.savedReports);
    selectItemsIfWithinLimits({dispatch, items: [...legendItems, ...items.filter(it => it.type !== Medium.unknown)]});
  };

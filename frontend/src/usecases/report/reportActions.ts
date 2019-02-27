import {find, uniqBy} from 'lodash';
import {createStandardAction} from 'typesafe-actions';
import {TemporalResolution} from '../../components/dates/dateModels';
import {Maybe} from '../../helpers/Maybe';
import {GetState} from '../../reducers/rootReducer';
import {firstUpperTranslated} from '../../services/translationService';
import {Medium} from '../../state/ui/graph/measurement/measurementModels';
import {showFailMessage} from '../../state/ui/message/messageActions';
import {SelectionInterval} from '../../state/user-selection/userSelectionModels';
import {Dispatcher, uuid} from '../../types/Types';
import {LegendItem, QuantityMedium} from './reportModels';
import {getLegendItems} from './reportSelectors';

export const addLegendItems = createStandardAction('ADD_LEGEND_ITEMS')<LegendItem[]>();
export const selectResolution = createStandardAction('SELECT_RESOLUTION')<TemporalResolution>();
export const toggleLine = createStandardAction('TOGGLE_LINE')<uuid>();
export const toggleQuantityByMedium = createStandardAction('TOGGLE_QUANTITY_BY_MEDIUM')<QuantityMedium>();
export const showHideAllByMedium = createStandardAction('SHOW_HIDE_ALL_BY_MEDIUM')<Medium>();
export const showHideMediumRows = createStandardAction('SHOW_HIDE_MEDIUM_ROWS')<Medium>();
export const removeAllByMedium = createStandardAction('REMOVE_ALL_BY_MEDIUM')<Medium>();
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
    const {report} = getState();
    const legendItems: LegendItem[] = getLegendItems(report);
    if (legendItem.medium !== Medium.unknown
        && find(legendItems, (it: LegendItem) => it.id === legendItem.id) === undefined) {
      const item: LegendItem = Maybe.maybe<LegendItem>(find(legendItems, {medium: legendItem.medium}))
        .map(it => ({...it, ...legendItem}))
        .orElse(legendItem);
      const items: LegendItem[] = [...legendItems, item];
      selectItemsIfWithinLimits({dispatch, items});
    }
  };

export const deleteItem = (id: uuid) =>
  (dispatch, getState: GetState) => {
    const legendItems = getLegendItems(getState().report);
    const items: LegendItem[] = legendItems.filter((item: LegendItem) => item.id !== id);
    if (legendItems.length !== items.length) {
      selectItemsIfWithinLimits({dispatch, items});
    }
  };

export const addAllToReport = (items: LegendItem[]) =>
  (dispatch, getState: GetState) => {
    const legendItems: LegendItem[] = getLegendItems(getState().report);
    selectItemsIfWithinLimits({dispatch, items: [...legendItems, ...items.filter(it => it.medium !== Medium.unknown)]});
  };

import {find} from 'lodash';
import {createStandardAction} from 'typesafe-actions';
import {TemporalResolution} from '../../components/dates/dateModels';
import {GetState} from '../../reducers/rootReducer';
import {firstUpperTranslated} from '../../services/translationService';
import {showFailMessage} from '../../state/ui/message/messageActions';
import {SelectionInterval} from '../../state/user-selection/userSelectionModels';
import {Dispatcher, emptyActionOf, payloadActionOf, uuid} from '../../types/Types';
import {LegendItem, SelectedReportPayload} from './reportModels';
import {getLegendItems, getSelectedReportPayload} from './reportSelectors';

export const SELECT_RESOLUTION = 'SELECT_RESOLUTION';
export const SET_SELECTED_ITEMS = 'SET_SELECTED_ITEMS';
export const TOGGLE_LINE = 'TOGGLE_LINE';
export const HIDE_ALL_LINES = 'HIDE_ALL_LINES';
export const REMOVE_SELECTED_LIST_ITEMS = 'REMOVE_SELECTED_LIST_ITEMS';

export const setSelectedItems = payloadActionOf<SelectedReportPayload>(SET_SELECTED_ITEMS);
export const selectResolution = payloadActionOf<TemporalResolution>(SELECT_RESOLUTION);
export const toggleLine = payloadActionOf<uuid>(TOGGLE_LINE);
export const hideAllLines = emptyActionOf(HIDE_ALL_LINES);
export const removeSelectedListItems = emptyActionOf(REMOVE_SELECTED_LIST_ITEMS);
export const setReportTimePeriod = createStandardAction('SET_REPORT_TIME_PERIOD')<SelectionInterval>();

export const limit: number = 100;

interface DispatchWithinLimits {
  dispatch: Dispatcher;
  items: LegendItem[];
}

const selectItemsIfWithinLimits = ({dispatch, items: legendItems}: DispatchWithinLimits) => {
  if (legendItems.length > limit) {
    dispatch(showFailMessage(firstUpperTranslated(
      'only {{limit}} meters can be selected at the same time', {limit},
    )));
    return;
  }
  dispatch(setSelectedItems(getSelectedReportPayload(legendItems)));
};

export const addToReport = (legendItem: LegendItem) =>
  (dispatch, getState: GetState) => {
    const {report} = getState();
    const legendItems: LegendItem[] = getLegendItems(report);
    if (find(legendItems, (it: LegendItem) => it.id === legendItem.id) === undefined) {
      const items: LegendItem[] = [...legendItems, legendItem];
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
    selectItemsIfWithinLimits({dispatch, items: [...legendItems, ...items]});
  };

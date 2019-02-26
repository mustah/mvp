import {find} from 'lodash';
import {createStandardAction} from 'typesafe-actions';
import {TemporalResolution} from '../../components/dates/dateModels';
import {Maybe} from '../../helpers/Maybe';
import {GetState} from '../../reducers/rootReducer';
import {firstUpperTranslated} from '../../services/translationService';
import {Medium} from '../../state/ui/graph/measurement/measurementModels';
import {showFailMessage} from '../../state/ui/message/messageActions';
import {SelectionInterval} from '../../state/user-selection/userSelectionModels';
import {Dispatcher, payloadActionOf, uuid} from '../../types/Types';
import {LegendItem, SelectedReportPayload} from './reportModels';
import {getLegendItems, getSelectedReportPayload} from './reportSelectors';

export const SELECT_RESOLUTION = 'SELECT_RESOLUTION';

export const setSelectedItems = createStandardAction('SET_SELECTED_ITEMS')<SelectedReportPayload>();
export const selectResolution = payloadActionOf<TemporalResolution>(SELECT_RESOLUTION);
export const toggleLine = createStandardAction('TOGGLE_LINE')<uuid>();
export const hideAllByMedium = createStandardAction('HIDE_ALL_BY_MEDIUM')<Medium>();
export const showHideMediumRows = createStandardAction('SHOW_HIDE_MEDIUM_ROWS')<Medium>();
export const removeAllByMedium = createStandardAction('REMOVE_ALL_BY_MEDIUM')<Medium>();
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
    selectItemsIfWithinLimits({dispatch, items: [...legendItems, ...items]});
  };

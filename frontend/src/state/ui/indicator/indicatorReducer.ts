import {EmptyAction} from 'react-redux-typescript';
import {Action} from '../../../types/Types';
import {LOGOUT_USER} from '../../../usecases/auth/authActions';
import {REMOVE_SELECTED_LIST_ITEMS, SET_SELECTED_ITEMS} from '../../../usecases/report/reportActions';
import {SelectedReportPayload} from '../../../usecases/report/reportModels';
import {RESET_SELECTION, SELECT_SAVED_SELECTION, SET_THRESHOLD} from '../../user-selection/userSelectionActions';
import {ThresholdQuery, UserSelection} from '../../user-selection/userSelectionModels';
import {Medium, Quantity} from '../graph/measurement/measurementModels';

export interface IndicatorState {
  selectedQuantities: Quantity[];
}

export const initialState: IndicatorState = {
  selectedQuantities: [],
};

type ActionTypes =
  | Action<Medium[] | Quantity[] | SelectedReportPayload | UserSelection | ThresholdQuery>
  | EmptyAction<string>;

export const indicator = (state: IndicatorState = initialState, action: ActionTypes): IndicatorState => {
  switch (action.type) {
    case SET_SELECTED_ITEMS:
      const {quantities}: SelectedReportPayload = (action as Action<SelectedReportPayload>).payload;
      return {
        ...state,
        selectedQuantities: quantities,
      };
    case SET_THRESHOLD:
      const thresholdQuery: ThresholdQuery = (action as Action<ThresholdQuery>).payload;
      return {
        ...state,
        selectedQuantities: [thresholdQuery.quantity],
      };
    case REMOVE_SELECTED_LIST_ITEMS:
    case SELECT_SAVED_SELECTION:
    case RESET_SELECTION:
    case LOGOUT_USER:
      return initialState;
    default:
      return state;
  }
};

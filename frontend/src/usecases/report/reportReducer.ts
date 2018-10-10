import {EmptyAction} from 'react-redux-typescript';
import {resetReducer} from '../../state/domain-models/domainModelsReducer';
import {SELECT_PERIOD} from '../../state/user-selection/userSelectionActions';
import {Action} from '../../types/Types';
import {LOGOUT_USER} from '../auth/authActions';
import {SelectedEntriesPayload, SET_SELECTED_ENTRIES} from './reportActions';
import {ReportState} from './reportModels';

export const initialState: ReportState = {
  selectedListItems: [],
};

type ActionTypes = Action<SelectedEntriesPayload> | Action<string[]> | EmptyAction<string>;

export const report = (state: ReportState = initialState, action: ActionTypes): ReportState => {
  switch (action.type) {
    case SET_SELECTED_ENTRIES:
      return {
        ...state,
        selectedListItems: [...(action as Action<SelectedEntriesPayload>).payload.ids],
      };
    case LOGOUT_USER:
      return {...initialState};
    case SELECT_PERIOD:
      return state;
    default:
      return resetReducer<ReportState>(state, action, {...initialState});
  }
};

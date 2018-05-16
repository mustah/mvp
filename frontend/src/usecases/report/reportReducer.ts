import {EmptyAction} from 'react-redux-typescript';
import {resetReducer} from '../../state/domain-models/domainModelsReducer';
import {Action, uuid} from '../../types/Types';
import {LOGOUT_USER} from '../auth/authActions';
import {SET_SELECTED_ENTRIES} from './reportActions';
import {ReportState} from './reportModels';

export const initialState: ReportState = {
  selectedListItems: [],
};

type ActionTypes = Action<uuid[]> | Action<string> | EmptyAction<string>;

export const report = (state: ReportState = initialState, action: ActionTypes): ReportState => {
  switch (action.type) {
    case SET_SELECTED_ENTRIES:
      return {
        ...state,
        selectedListItems: (action as Action<uuid[]>).payload,
      };
    case LOGOUT_USER:
      return {...initialState};
    default:
      return resetReducer<ReportState>(state, action, {...initialState});
  }
};

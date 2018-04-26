import {selectionWasChanged} from '../../state/domain-models/domainModelsReducer';
import {Action, uuid} from '../../types/Types';
import {SET_SELECTED_ENTRIES} from './reportActions';
import {ReportState} from './reportModels';

export const initialState: ReportState = {
  selectedListItems: [],
};

type ActionTypes = Action<uuid[]> | Action<string>;

export const report = (state: ReportState = initialState, action: ActionTypes): ReportState => {
  if (selectionWasChanged(action.type)) {
    return {...initialState};
  }

  switch (action.type) {
    case SET_SELECTED_ENTRIES:
      return {
        ...state,
        selectedListItems: (action as Action<uuid[]>).payload,
      };
    default:
      return state;
  }
};

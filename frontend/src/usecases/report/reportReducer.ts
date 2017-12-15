import {Action, uuid} from '../../types/Types';
import {ReportState} from './reportModels';
import {SET_SELECTED_ENTRIES} from './reportActions';

const initialState: ReportState = {
  selectedListItems: [],
};

type ActionTypes = Action<uuid[]>;

export const report = (state: ReportState = initialState, action: ActionTypes): ReportState => {
  switch (action.type) {
    case SET_SELECTED_ENTRIES:
      return {
        ...state,
        selectedListItems: action.payload,
      };
    default:
      return state;
  }
};

import {AnyAction} from 'redux';
import {ReportState} from './models/reportModels';
import {SET_SELECTED_ENTRIES} from './reportActions';

const initialState: ReportState = {
  selectedListItems: [],
};

export const report = (state: ReportState = initialState, action: AnyAction): ReportState => {
  const {payload} = action;
  switch (action.type) {
    case SET_SELECTED_ENTRIES:
      return {
        ...state,
        selectedListItems: payload,
      };
    default:
      return state;
  }
};

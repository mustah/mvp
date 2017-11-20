import {AnyAction} from 'redux';
import {ReportState} from './models/reportModels';
import {SELECT_ENTRY_TOGGLE} from './reportActions';

const initialState: ReportState = {
  selectedListItems: [],
};

export const report = (state: ReportState = initialState, action: AnyAction): ReportState => {
  const {payload} = action;
  switch (action.type) {
    case SELECT_ENTRY_TOGGLE:
      return {
        ...state,
        selectedListItems: payload,
      };
    default:
      return state;
  }
};

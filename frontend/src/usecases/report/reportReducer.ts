import {AnyAction} from 'redux';
import {ReportState} from './models/ReportModels';
import {REPORTS_REQUEST} from './reportActions';

const initialState: ReportState = {
  title: 'ReportState',
  records: [],
  isFetching: false,
};

export const report = (state: ReportState = initialState, action: AnyAction): ReportState => {
  switch (action.type) {
    case REPORTS_REQUEST:
      return {
        ...state,
        isFetching: true,
      };
    default:
      return state;
  }
};

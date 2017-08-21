import {AnyAction} from 'redux';
import {DATA_ANALYSIS_REQUEST} from '../../types/ActionTypes';
import {DataAnalysisState} from './models/DataAnalysis';

const initialState: DataAnalysisState = {
  title: 'DataAnalysisState',
  records: [],
  isFetching: false,
};

export const dataAnalysis = (state: DataAnalysisState = initialState, action: AnyAction): DataAnalysisState => {
  switch (action.type) {
    case DATA_ANALYSIS_REQUEST:
      return {
        ...state,
        isFetching: true,
      };
    default:
      return state;
  }
};

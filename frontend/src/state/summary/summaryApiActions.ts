import {GetState} from '../../reducers/rootReducer';
import {EndPoints} from '../../services/endPoints';
import {fetchIfNeeded, FetchIfNeeded} from '../api/apiActions';
import {SelectionSummary, SummaryState} from './summaryModels';

const shouldFetchSummary: FetchIfNeeded = (getState: GetState): boolean => {
  const summary: SummaryState = getState().summary;
  return !summary.isSuccessfullyFetched && !summary.error && !summary.isFetching;
};

export const fetchSummary = fetchIfNeeded<SelectionSummary>(
  EndPoints.summary,
  shouldFetchSummary,
);

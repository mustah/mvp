import {GetState} from '../../reducers/rootReducer';
import {EndPoints} from '../../services/endPoints';
import {FetchIfNeeded, fetchIfNeeded} from '../api/apiActions';
import {NormalizedSelectionTree} from './selectionTreeModels';
import {selectionTreeDataFormatter} from './selectionTreeSchemas';

const shouldFetch: FetchIfNeeded = (getState: GetState): boolean => {
  const {selectionTree: {isFetching, isSuccessfullyFetched, error}} = getState();
  return !isSuccessfullyFetched && !error && !isFetching;
};

export const fetchSelectionTree = fetchIfNeeded<NormalizedSelectionTree>(
  EndPoints.selectionTree,
  shouldFetch,
  selectionTreeDataFormatter,
);

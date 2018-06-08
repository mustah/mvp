import {AxiosPromise} from 'axios';
import {normalize, Schema} from 'normalizr';
import {Dispatch} from 'react-redux';
import {InvalidToken} from '../../exceptions/InvalidToken';
import {makeUrl} from '../../helpers/urlFactory';
import {GetState, RootState} from '../../reducers/rootReducer';
import {EndPoints} from '../../services/endPoints';
import {isTimeoutError, restClient, wasRequestCanceled} from '../../services/restClient';
import {logout} from '../../usecases/auth/authActions';
import {
  makeActionsOf,
  noInternetConnection,
  RequestHandler,
  requestTimeout,
  responseMessageOrFallback,
} from '../api/apiActions';
import {NormalizedSelectionTree, SelectionTreeState} from './selectionTreeModels';
import {selectionTreeSchema} from './selectionTreeSchemas';

interface AsyncRequest<P> extends RequestHandler<P> {
  onRequest: (parameters?: string) => AxiosPromise<any>;
  formatData: (data: any) => NormalizedSelectionTree;
  parameters?: string;
  dispatch: Dispatch<RootState>;
}

const makeAsyncRequest = async <P>(
  {
    request,
    success,
    failure,
    onRequest,
    formatData = (id) => id,
    parameters,
    dispatch,
  }: AsyncRequest<P>) => {
  try {
    dispatch(request());
    const {data} = await onRequest(parameters);
    dispatch(success(formatData(data)));
  } catch (error) {
    if (error instanceof InvalidToken) {
      await dispatch(logout(error));
    } else if (isTimeoutError(error)) {
      dispatch(failure(requestTimeout()));
    } else if (!error.response) {
      dispatch(failure(noInternetConnection()));
    } else if (wasRequestCanceled(error)) {
      return;
    } else {
      dispatch(failure(responseMessageOrFallback(error.response)));
    }
  }
};

const shouldFetch = (selectionTree: SelectionTreeState): boolean =>
  !selectionTree.isSuccessfullyFetched && !selectionTree.error && !selectionTree.isFetching;

// TODO[!must!] remove all fetchIfNeeded code and use composition instead!

const fetchIfNeeded = <P>(endPoint: EndPoints, schema: Schema) => {
  const onRequest = (parameters?: string) =>
    restClient.get(makeUrl(endPoint, parameters));

  const formatData = (data: any) => normalize(data, schema);

  return (parameters?: string) =>
    (dispatch, getState: GetState) => {
      if (shouldFetch(getState().selectionTree)) {
        return makeAsyncRequest<P>({
          ...makeActionsOf<P>(endPoint),
          onRequest,
          formatData,
          parameters,
          dispatch,
        });
      }
      return null;
    };
};

export const fetchSelectionTree = fetchIfNeeded<NormalizedSelectionTree>(
  EndPoints.selectionTree,
  selectionTreeSchema,
);

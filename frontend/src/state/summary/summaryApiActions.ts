import {AxiosPromise} from 'axios';
import {Dispatch} from 'react-redux';
import {makeUrl} from '../../helpers/urlFactory';
import {GetState, RootState} from '../../reducers/rootReducer';
import {EndPoints} from '../../services/endPoints';
import {InvalidToken, restClient} from '../../services/restClient';
import {firstUpperTranslated} from '../../services/translationService';
import {
  Action,
  emptyActionOf,
  EncodedUriParameters,
  ErrorResponse,
  OnEmptyAction,
  OnPayloadAction,
  payloadActionOf,
} from '../../types/Types';
import {logout} from '../../usecases/auth/authActions';
import {SelectionSummary, SummaryState} from './summaryModels';

export interface RequestHandler<P> {
  request: OnEmptyAction;
  success: OnPayloadAction<P>;
  failure: (error: ErrorResponse) => Action<ErrorResponse>;
}

interface AsyncRequest<P> extends RequestHandler<P> {
  onRequest: (parameters?: EncodedUriParameters) => AxiosPromise<P>;
  parameters?: EncodedUriParameters;
  dispatch: Dispatch<RootState>;
}

const responseMessageOrFallback = (response): ErrorResponse => response && response.data ||
  {message: firstUpperTranslated('an unexpected error occurred')};

const makeAsyncRequest = async <P>(
  {
    request,
    success,
    failure,
    onRequest,
    parameters,
    dispatch,
  }: AsyncRequest<P>) => {
  dispatch(request());
  try {
    const {data} = await onRequest(parameters);
    dispatch(success(data));
  } catch (error) {
    if (error instanceof InvalidToken) {
      await dispatch(logout(error));
    } else {
      dispatch(failure(responseMessageOrFallback(error.response)));
    }
  }
};

const shouldFetch = (summary: SummaryState): boolean =>
  !summary.isSuccessfullyFetched && !summary.error && !summary.isFetching;

const fetchIfNeeded = <P>(endPoint: EndPoints) => {
  const onRequest = (parameters?: EncodedUriParameters): AxiosPromise<P> =>
    restClient.get(makeUrl(endPoint, parameters));

  return (parameters?: EncodedUriParameters) =>
    (dispatch, getState: GetState) => {
      if (shouldFetch(getState().summary)) {
        return makeAsyncRequest<P>({
          ...makeActionsOf<P>(endPoint),
          onRequest,
          parameters,
          dispatch,
        });
      }
      return null;
    };
};

export const requestAction = (endPoint: EndPoints): string => `REQUEST${endPoint}`;
export const successAction = (endPoint: EndPoints): string => `SUCCESS${endPoint}`;
export const failureAction = (endPoint: EndPoints): string => `FAILURE${endPoint}`;

export const makeActionsOf = <P>(endPoint: EndPoints): RequestHandler<P> => ({
  request: emptyActionOf(requestAction(endPoint)),
  success: payloadActionOf<P>(successAction(endPoint)),
  failure: payloadActionOf<ErrorResponse>(failureAction(endPoint)),
});

export const fetchSummary = fetchIfNeeded<SelectionSummary>(EndPoints.summaryMeters);

import {AxiosPromise} from 'axios';
import {Dispatch} from 'redux';
import {InvalidToken} from '../../exceptions/InvalidToken';
import {makeUrl} from '../../helpers/urlFactory';
import {GetState, RootState} from '../../reducers/rootReducer';
import {EndPoints} from '../../services/endPoints';
import {isTimeoutError, restClient, wasRequestCanceled} from '../../services/restClient';
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

export const responseMessageOrFallback = (response?: any): ErrorResponse =>
  (response && response.data)
  || {message: firstUpperTranslated('an unexpected error occurred')};

export const noInternetConnection = (): ErrorResponse => ({
  message: firstUpperTranslated('the internet connection appears to be offline'),
});

export const requestTimeout = (): ErrorResponse => ({
  message: firstUpperTranslated(
    'looks like the server is taking to long to respond, please try again in soon'),
});

const makeAsyncRequest = async <P>(
  {
    request,
    success,
    failure,
    onRequest,
    parameters,
    dispatch,
  }: AsyncRequest<P>) => {
  try {
    dispatch(request());
    const {data} = await onRequest(parameters);
    dispatch(success(data));
  } catch (error) {
    if (error instanceof InvalidToken) {
      await dispatch(logout(error));
    } else if (wasRequestCanceled(error)) {
      return;
    } else if (isTimeoutError(error)) {
      dispatch(failure(requestTimeout()));
    } else if (!error.response) {
      dispatch(failure(noInternetConnection()));
    } else {
      dispatch(failure(responseMessageOrFallback(error.response)));
    }
  }
};

export type FetchIfNeeded = (getState: GetState) => boolean;

export const requestAction = (endPoint: EndPoints): string => `REQUEST${endPoint}`;
export const successAction = (endPoint: EndPoints): string => `SUCCESS${endPoint}`;
export const failureAction = (endPoint: EndPoints): string => `FAILURE${endPoint}`;

export const makeActionsOf = <P>(endPoint: EndPoints): RequestHandler<P> => ({
  request: emptyActionOf(requestAction(endPoint)),
  success: payloadActionOf<P>(successAction(endPoint)),
  failure: payloadActionOf<ErrorResponse>(failureAction(endPoint)),
});

export const fetchIfNeeded = <P>(endPoint: EndPoints, fetchIfNeeded: FetchIfNeeded) => {
  const onRequest = (parameters?: EncodedUriParameters): AxiosPromise<P> =>
    restClient.get(makeUrl(endPoint, parameters));

  return (parameters?: EncodedUriParameters) =>
    (dispatch, getState: GetState) => {
      if (fetchIfNeeded(getState)) {
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

import {AxiosPromise} from 'axios';
import {Dispatch} from 'redux';
import {InvalidToken} from '../../exceptions/InvalidToken';
import {identityType} from '../../helpers/commonHelpers';
import {makeUrl} from '../../helpers/urlFactory';
import {GetState, RootState} from '../../reducers/rootReducer';
import {EndPoints} from '../../services/endPoints';
import {isTimeoutError, restClient, wasRequestCanceled} from '../../services/restClient';
import {firstUpperTranslated} from '../../services/translationService';
import {
  Action,
  ActionKey,
  emptyActionOf,
  EncodedUriParameters,
  ErrorResponse,
  OnEmptyAction,
  OnFetch,
  OnPayloadAction,
  OnUpdate,
  payloadActionOf,
} from '../../types/Types';
import {logout} from '../../usecases/auth/authActions';
import {DataFormatter} from '../domain-models/domainModelsActions';

export interface RequestHandler<P> {
  request: OnEmptyAction;
  success: OnPayloadAction<P>;
  failure: (error: ErrorResponse) => Action<ErrorResponse>;
}

interface AsyncRequest<P, REQUEST_BODY> extends RequestHandler<P> {
  dispatch: Dispatch<RootState>;
  onRequest: (body?: REQUEST_BODY) => AxiosPromise<P>;
  formatData?: DataFormatter<P>;
  parameters?: EncodedUriParameters;
  body?: REQUEST_BODY;
}

export const responseMessageOrFallback = (response?: any): ErrorResponse => {
  if (response && response.data) {
    const {data} = response;
    if (data.message) {
      return {...data, message: firstUpperTranslated(data.message.toLowerCase())};
    } else {
      return data;
    }
  }
  return {message: firstUpperTranslated('an unexpected error occurred')};
};

export const noInternetConnection = (): ErrorResponse => ({
  message: firstUpperTranslated('the internet connection appears to be offline'),
});

export const requestTimeout = (): ErrorResponse => ({
  message: firstUpperTranslated(
    'looks like the server is taking to long to respond, please try again in soon'),
});

const makeAsyncRequest = async <P, REQUEST_BODY>({
  request,
  success,
  failure,
  onRequest,
  formatData = identityType,
  dispatch,
  body,
}: AsyncRequest<P, REQUEST_BODY>) => {
  try {
    dispatch(request());
    const {data} = await onRequest(body);
    dispatch(success(formatData(data)));
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
export type ActionsFactory<P> = (actionKey: ActionKey) => RequestHandler<P>;

export const requestAction = (actionKey: ActionKey): string => `REQUEST_${actionKey}`;
export const successAction = (actionKey: ActionKey): string => `SUCCESS_${actionKey}`;
export const failureAction = (actionKey: ActionKey): string => `FAILURE_${actionKey}`;
export const clearAction = (actionKey: ActionKey): string => `CLEAR_${actionKey}`;

export const makeActionsOf = <P>(actionKey: ActionKey): RequestHandler<P> => ({
  request: emptyActionOf(requestAction(actionKey)),
  success: payloadActionOf<P>(successAction(actionKey)),
  failure: payloadActionOf<ErrorResponse>(failureAction(actionKey)),
});

export const fetchIfNeeded = <P>(
  actionKey: ActionKey,
  fetchIfNeeded: FetchIfNeeded,
  actionsFactory: ActionsFactory<P> = makeActionsOf,
  formatData?: DataFormatter<P>,
): OnFetch =>
  (endPoint: EndPoints | string, parameters?: EncodedUriParameters) =>
    (dispatch, getState: GetState) => {
      if (fetchIfNeeded(getState)) {
        return makeAsyncRequest<P, P>({
          ...actionsFactory(actionKey),
          onRequest: (): AxiosPromise<P> => restClient.get(makeUrl(endPoint, parameters)),
          formatData,
          dispatch,
        });
      }
      return null;
    };

export const putRequest = <P, REQUEST_BODY>(
  actionKey: ActionKey,
  actionsFactory: ActionsFactory<P> = makeActionsOf,
  formatData?: DataFormatter<P>,
): OnUpdate<REQUEST_BODY> =>
  (endPoint: EndPoints | string, body: REQUEST_BODY) =>
    (dispatch) =>
      makeAsyncRequest<P, REQUEST_BODY>({
        ...actionsFactory(actionKey),
        onRequest: (payload: REQUEST_BODY): AxiosPromise<P> => restClient.put(endPoint, payload),
        formatData,
        dispatch,
        body,
      });

import {InvalidToken} from '../../exceptions/InvalidToken';
import {EndPoints} from '../../services/endPoints';
import {isTimeoutError, restClient, wasRequestCanceled} from '../../services/restClient';
import {firstUpperTranslated} from '../../services/translationService';
import {
  noInternetConnection,
  requestTimeout,
  responseMessageOrFallback,
} from '../../state/api/apiActions';
import {showFailMessage, showSuccessMessage} from '../../state/ui/message/messageActions';
import {Dispatcher, uuid} from '../../types/Types';
import {logout} from '../auth/authActions';

const onError = (dispatch: Dispatcher, error): void => {
  if (error instanceof InvalidToken) {
    dispatch(logout(error));
  } else if (isTimeoutError(error)) {
    dispatch(showFailMessage(requestTimeout().message));
  } else if (!error.response) {
    dispatch(showFailMessage(noInternetConnection().message));
  } else if (wasRequestCanceled(error)) {
    return;
  } else {
    dispatch(showFailMessage(responseMessageOrFallback(error.response).message));
  }
};

export const syncWithMetering = (logicalMeterId: uuid) =>
  async (dispatch: Dispatcher) => {
    try {
      await restClient.post(`${EndPoints.meters}/${logicalMeterId}/synchronize`);
      const message = firstUpperTranslated('meter will soon be synchronized');
      dispatch(showSuccessMessage(message));
    } catch (error) {
      onError(dispatch, error);
    }
  };

export const syncAllMeters = (ids: uuid[]) =>
  async (dispatch: Dispatcher) => {
    try {
      if (ids.length > 0) {
        await restClient.post(`${EndPoints.meters}/synchronize`, ids);
        const count = ids.length;
        const message = firstUpperTranslated('{{count}} meter will soon be synchronized', {count});
        dispatch(showSuccessMessage(message));
      }
    } catch (error) {
      onError(dispatch, error);
    }
  };

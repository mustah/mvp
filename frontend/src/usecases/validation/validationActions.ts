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
import {uuid} from '../../types/Types';
import {logout} from '../auth/authActions';

export const syncWithMetering = (logicalMeterId: uuid) => {
  return async (dispatch) => {
    try {
      await restClient.post(`${EndPoints.meters}/${logicalMeterId}/synchronize`);
      dispatch(showSuccessMessage(firstUpperTranslated('meter will soon be synchronized')));
    } catch (error) {
      if (error instanceof InvalidToken) {
        await dispatch(logout(error));
      } else if (isTimeoutError(error)) {
        dispatch(showFailMessage(requestTimeout().message));
      } else if (!error.response) {
        dispatch(showFailMessage(noInternetConnection().message));
      } else if (wasRequestCanceled(error)) {
        return;
      } else {
        dispatch(showFailMessage(responseMessageOrFallback(error.response).message));
      }
    }
  };
};

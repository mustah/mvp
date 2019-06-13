import {Dispatch} from 'redux';
import {InvalidToken} from '../../exceptions/InvalidToken';
import {EndPoints} from '../../services/endPoints';
import {isTimeoutError, restClient, wasRequestCanceled} from '../../services/restClient';
import {firstUpperTranslated} from '../../services/translationService';
import {noInternetConnection, requestTimeout, responseMessageOrFallback} from '../../state/api/apiActions';
import {showFailMessage, showSuccessMessage} from '../../state/ui/message/messageActions';
import {uuid} from '../../types/Types';
import {logout} from '../auth/authActions';

const onError = (dispatch: Dispatch, error): void => {
  if (error instanceof InvalidToken) {
    dispatch(logout(error) as any);
  } else if (wasRequestCanceled(error)) {
    return;
  } else if (isTimeoutError(error)) {
    dispatch(showFailMessage(requestTimeout().message));
  } else if (!error.response) {
    dispatch(showFailMessage(noInternetConnection().message));
  } else {
    dispatch(showFailMessage(responseMessageOrFallback(error.response).message));
  }
};

export const syncWithMetering = (logicalMeterId: uuid) =>
  async (dispatch: Dispatch) => {
    try {
      await restClient.post(`${EndPoints.syncMeters}/${logicalMeterId}`);
      const message = firstUpperTranslated('meter will soon be synchronized');
      dispatch(showSuccessMessage(message));
    } catch (error) {
      onError(dispatch, error);
    }
  };

export const syncMeters = (ids: uuid[]) =>
  async (dispatch: Dispatch) => {
    try {
      if (ids.length > 0) {
        await restClient.post(`${EndPoints.syncMeters}`, ids);
        const count = ids.length;
        const message = firstUpperTranslated('{{count}} meter will soon be synchronized', {count});
        dispatch(showSuccessMessage(message));
      }
    } catch (error) {
      onError(dispatch, error);
    }
  };

export const syncMetersOrganisation = (organisationId: uuid) =>
  async (dispatch: Dispatch) => {
    try {
      const message = firstUpperTranslated('all meters for organisation will soon be synchronized');
      dispatch(showSuccessMessage(message));
      await restClient.post(`${EndPoints.syncMetersOrganisation}?id=${organisationId}`);
    } catch (error) {
      onError(dispatch, error);
    }
  };

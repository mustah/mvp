import axios from 'axios';
import {routerActions} from 'react-router-redux';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {routes} from '../../../app/routes';
import {InvalidToken} from '../../../exceptions/InvalidToken';
import {initTranslations} from '../../../i18n/__tests__/i18nMock';
import {RootState} from '../../../reducers/rootReducer';
import {EndPoints} from '../../../services/endPoints';
import {authenticate} from '../../../services/restClient';
import {firstUpperTranslated} from '../../../services/translationService';
import {Role, User} from '../../../state/domain-models/user/userModels';
import {showFailMessage, showSuccessMessage} from '../../../state/ui/message/messageActions';
import {ErrorResponse, uuid} from '../../../types/Types';
import {logoutUser} from '../../auth/authActions';
import {syncWithMetering} from '../validationActions';
import MockAdapter = require('axios-mock-adapter');

describe('syncWithMetering', () => {

  let store;
  let mockRestClient;
  const user: User = {
    id: 2,
    name: 'test user',
    email: 'test@test.se',
    language: 'en',
    organisation: {id: 1, name: 'elvaco', slug: 'elvaco'},
    roles: [Role.USER],
  };

  const logicalMeterId: uuid = 123;

  initTranslations({
    code: 'en',
    translation: {
      'meter will soon be synchronized': 'meter will soon be synchronized',
    },
  });

  beforeEach(() => {
    mockRestClient = new MockAdapter(axios);
    authenticate('test');
    const state: Partial<RootState> = {auth: {user, isAuthenticated: true}};
    store = configureStore([thunk])(state);
  });

  afterEach(() => {
    mockRestClient.reset();
  });

  describe('invalid token', () => {

    it('dispatches a logout action if token is invalid', async () => {
      const error = new InvalidToken('Token missing or invalid');

      const onSyncWithMeteringError = async () => {
        mockRestClient
          .onPost(`${EndPoints.meters}/${logicalMeterId}/synchronize`)
          .reply(401, error);
        mockRestClient.onGet(EndPoints.logout).reply(204);
        return store.dispatch(syncWithMetering(logicalMeterId));
      };

      await onSyncWithMeteringError();

      expect(store.getActions()).toEqual([
        logoutUser(error),
        routerActions.push(`${routes.login}/elvaco`),
      ]);
    });
  });

  describe('valid token', () => {

    it('dispatches a failure error message when request fails', async () => {
      const message = 'some unknown error';
      const error: Partial<ErrorResponse> = {message};

      const onSyncWithMeteringUnknownErrorMessage = async () => {
        mockRestClient
          .onPost(`${EndPoints.meters}/${logicalMeterId}/synchronize`)
          .reply(401, error);
        return store.dispatch(syncWithMetering(logicalMeterId));
      };

      await onSyncWithMeteringUnknownErrorMessage();

      expect(store.getActions()).toEqual([
        showFailMessage(message),
      ]);
    });

    it('displays success toast message', async () => {
      const onSyncWithMetering = async () => {
        mockRestClient
          .onPost(`${EndPoints.meters}/${logicalMeterId}/synchronize`)
          .reply(202);
        return store.dispatch(syncWithMetering(logicalMeterId));
      };

      await onSyncWithMetering();

      expect(store.getActions()).toEqual([
        showSuccessMessage(firstUpperTranslated('meter will soon be synchronized')),
      ]);
    });
  });
});

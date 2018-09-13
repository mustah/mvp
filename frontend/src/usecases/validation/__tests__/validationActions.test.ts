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
import {noInternetConnection, requestTimeout} from '../../../state/api/apiActions';
import {Role, User} from '../../../state/domain-models/user/userModels';
import {showFailMessage, showSuccessMessage} from '../../../state/ui/message/messageActions';
import {Callback, ErrorResponse, uuid} from '../../../types/Types';
import {logoutUser} from '../../auth/authActions';
import {syncAllMeters, syncWithMetering} from '../validationActions';
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

  const url = `${EndPoints.syncMeters}/${logicalMeterId}`;
  const syncAllUrl = `${EndPoints.syncMeters}`;

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

      await makeRequest(() => {
        mockRestClient.onPost(url).reply(401, error);
        mockRestClient.onGet(EndPoints.logout).reply(204);
      });

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

      await makeRequest(() => mockRestClient.onPost(url).reply(401, error));

      expect(store.getActions()).toEqual([
        showFailMessage(message),
      ]);
    });

    it('displays success toast message', async () => {
      await makeRequest(() => mockRestClient.onPost(url).reply(202));

      expect(store.getActions()).toEqual([
        showSuccessMessage(firstUpperTranslated('meter will soon be synchronized')),
      ]);
    });

  });

  describe('network error', () => {

    it('display error message when there is not internet connection', async () => {
      await makeRequest(() => mockRestClient.onPost(url).networkError());

      expect(store.getActions()).toEqual([
        showFailMessage(noInternetConnection().message),
      ]);
    });
  });

  describe('request timeout', () => {

    it('display error message when the request times out', async () => {
      await makeRequest(() => mockRestClient.onPost(url).timeout());

      expect(store.getActions()).toEqual([showFailMessage(requestTimeout().message)]);
    });

    it('displays error message when the request times out when syncing all meters', async () => {
      await onSyncAllMeters(() => mockRestClient.onPost(syncAllUrl).timeout(), [1]);

      expect(store.getActions()).toEqual([showFailMessage(requestTimeout().message)]);
    });
  });

  describe('sync all meters', () => {

    it('does not sync when there are no meter ids', async () => {
      await onSyncAllMeters(() => mockRestClient.onPost(syncAllUrl).reply(200, {}), []);

      expect(store.getActions()).toEqual([]);
    });

    it('syncs with all meters', async () => {
      await onSyncAllMeters(() => mockRestClient.onPost(syncAllUrl).reply(200, {}), [1, 2, 3]);

      expect(store.getActions()).toEqual([
        showSuccessMessage('3 meter will soon be synchronized'),
      ]);
    });
  });

  const makeRequest = async (mockRequestCallbacks: Callback) => {
    mockRequestCallbacks();
    return store.dispatch(syncWithMetering(logicalMeterId));
  };

  const onSyncAllMeters = async (mockRequestCallbacks: Callback, ids: uuid[]) => {
    mockRequestCallbacks();
    return store.dispatch(syncAllMeters(ids));
  };

});

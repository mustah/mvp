import axios from 'axios';
import {default as MockAdapter} from 'axios-mock-adapter';
import {routerActions} from 'react-router-redux';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {makeUser} from '../../../__tests__/testDataFactory';
import {routes} from '../../../app/routes';
import {InvalidToken} from '../../../exceptions/InvalidToken';
import {initTranslations} from '../../../i18n/__tests__/i18nMock';
import {RootState} from '../../../reducers/rootReducer';
import {EndPoints} from '../../../services/endPoints';
import {authenticate} from '../../../services/restClient';
import {firstUpperTranslated} from '../../../services/translationService';
import {noInternetConnection, requestTimeout} from '../../../state/api/apiActions';
import {User} from '../../../state/domain-models/user/userModels';
import {showFailMessage, showSuccessMessage} from '../../../state/ui/message/messageActions';
import {Callback, ErrorResponse, uuid} from '../../../types/Types';
import {logoutUser} from '../../auth/authActions';
import {syncMeters, syncWithMetering} from '../meterActions';

describe('syncWithMetering', () => {

  let store;
  let mockRestClient;
  const user: User = makeUser();

  const logicalMeterId: uuid = 123;

  const url = `${EndPoints.syncMeters}/${logicalMeterId}`;
  const syncMetersUrl = `${EndPoints.syncMeters}`;

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
        routerActions.push(`${routes.login}/daily-planet`),
      ]);
    });
  });

  describe('valid token', () => {

    it('dispatches a failure error message when request fails', async () => {
      const message = 'Some unknown error';
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
      await onSyncMeters(() => mockRestClient.onPost(syncMetersUrl).timeout(), [1]);

      expect(store.getActions()).toEqual([showFailMessage(requestTimeout().message)]);
    });
  });

  describe('sync all meters', () => {

    it('does not sync when there are no meter ids', async () => {
      await onSyncMeters(() => mockRestClient.onPost(syncMetersUrl).reply(200, {}), []);

      expect(store.getActions()).toEqual([]);
    });

    it('syncs with all meters', async () => {
      await onSyncMeters(() => mockRestClient.onPost(syncMetersUrl).reply(200, {}), [1, 2, 3]);

      expect(store.getActions()).toEqual([
        showSuccessMessage('3 meter will soon be synchronized'),
      ]);
    });
  });

  const makeRequest = async (mockRequestCallbacks: Callback) => {
    mockRequestCallbacks();
    return store.dispatch(syncWithMetering(logicalMeterId));
  };

  const onSyncMeters = async (mockRequestCallbacks: Callback, ids: uuid[]) => {
    mockRequestCallbacks();
    return store.dispatch(syncMeters(ids));
  };

});

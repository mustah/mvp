import {routerActions} from 'react-router-redux';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {routes} from '../../../app/routes';
import {makeUrl} from '../../../helpers/urlFactory';
import {initLanguage} from '../../../i18n/i18n';
import {EndPoints} from '../../../services/endPoints';
import {InvalidToken, restClient, restClientWith} from '../../../services/restClient';
import {ErrorResponse} from '../../../types/Types';
import {logoutUser} from '../../../usecases/auth/authActions';
import {Unauthorized} from '../../../usecases/auth/authModels';
import {User} from '../../domain-models/user/userModels';
import {failureAction, fetchSummary, requestAction, successAction} from '../summaryApiActions';
import {SelectionSummary} from '../summaryModels';
import {initialState} from '../summaryReducer';
import MockAdapter = require('axios-mock-adapter');

describe('summaryApiActions', () => {

  initLanguage({code: 'en', name: 'english'});

  let store;
  let mockRestClient;
  const configureMockStore = configureStore([thunk]);

  beforeEach(() => {
    restClientWith('someToken');
    mockRestClient = new MockAdapter(restClient);
  });

  describe('token has expired or is invalid', () => {

    it('redirect user to login page when token is invalid', async () => {
      const user: User = {
        id: 1,
        name: 'al',
        email: 'al@la.se',
        organisation: {id: 1, name: 'elvaco', slug: 'elvaco'},
        roles: [],
      };
      const initialRootState = {
        auth: {
          user,
          isAuthenticated: true,
        },
        summary: {...initialState},
      };
      store = configureMockStore(initialRootState);
      const error = new InvalidToken('Token missing or invalid');

      const onFetchMeterSummaryWithInvalidToken = async () => {
        mockRestClient.onGet(EndPoints.summaryMeters).reply(401, error);
        mockRestClient.onGet(EndPoints.logout).reply(204);
        return store.dispatch(fetchSummary());
      };

      await onFetchMeterSummaryWithInvalidToken();

      expect(store.getActions()).toEqual([
        {
          type: requestAction(EndPoints.summaryMeters),
        },
        logoutUser(error as Unauthorized),
        routerActions.push(`${routes.login}/${initialRootState.auth.user.organisation.slug}`),
      ]);
    });
  });

  describe('fetch initial summary for all meters', () => {

    it('when fetching meter summary fails', async () => {
      store = configureMockStore({summary: {...initialState}});

      const onFetchMeterSummaryFail = async () => {
        const response: ErrorResponse = {message: 'request failed'};
        mockRestClient.onGet(makeUrl(EndPoints.summaryMeters)).reply(401, response);
        return store.dispatch(fetchSummary());
      };

      await onFetchMeterSummaryFail();

      expect(store.getActions()).toEqual([
        {
          type: requestAction(EndPoints.summaryMeters),
        },
        {
          type: failureAction(EndPoints.summaryMeters),
          payload: {message: 'request failed'},
        },
      ]);
    });

    it('fetches summary action on initial load', async () => {
      store = configureMockStore({summary: {...initialState}});

      await onFetchMeterSummary();

      expect(store.getActions()).toEqual([
        {
          type: requestAction(EndPoints.summaryMeters),
        },
        {
          type: successAction(EndPoints.summaryMeters),
          payload: {numMeters: 2, numCities: 1, numAddresses: 2},
        }]);
    });
  });

  describe('fetch with parameters', () => {

    it('does not fetch again when successfully fetched', async () => {
      store = configureMockStore({
        summary: {
          isFetching: false,
          isSuccessfullyFetched: true,
          payload: {numMeters: 2, numCities: 1, numAddresses: 2},
        },
      });

      await onFetchMeterSummary('id=2');

      expect(store.getActions()).toEqual([]);
    });

    it('does not fetch when already fetching', async () => {
      store = configureMockStore({
        summary: {
          isFetching: true,
          isSuccessfullyFetched: false,
          payload: initialState.payload,
        },
      });

      await onFetchMeterSummary('id=2');

      expect(store.getActions()).toEqual([]);
    });

    it('does not fetch when fetch summary has errors', async () => {
      store = configureMockStore({
        summary: {
          isFetching: false,
          isSuccessfullyFetched: false,
          error: {message: 'has error'},
        },
      });

      await onFetchMeterSummary('id=2');

      expect(store.getActions()).toEqual([]);
    });
  });

  const onFetchMeterSummary = async (parameters?: string) => {
    const response: SelectionSummary = {numMeters: 2, numCities: 1, numAddresses: 2};
    mockRestClient.onGet(makeUrl(EndPoints.summaryMeters, parameters)).reply(200, response);
    return store.dispatch(fetchSummary(parameters));
  };
});
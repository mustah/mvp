import {default as MockAdapter} from 'axios-mock-adapter';
import {routerActions} from 'react-router-redux';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {makeUser} from '../../../__tests__/testDataFactory';
import {routes} from '../../../app/routes';
import {InvalidToken} from '../../../exceptions/InvalidToken';
import {makeUrl} from '../../../helpers/urlFactory';
import {initTranslations} from '../../../i18n/__tests__/i18nMock';
import {EndPoints} from '../../../services/endPoints';
import {restClient, restClientWith} from '../../../services/restClient';
import {EncodedUriParameters, ErrorResponse} from '../../../types/Types';
import {logoutUser} from '../../../usecases/auth/authActions';
import {Unauthorized} from '../../../usecases/auth/authModels';
import {makeActionsOf, requestTimeout} from '../../api/apiActions';
import {User} from '../../domain-models/user/userModels';
import {fetchSummary} from '../summaryApiActions';
import {SelectionSummary} from '../summaryModels';
import {initialState} from '../summaryReducer';

describe('summaryApiActions', () => {

  initTranslations({
    code: 'en',
    translation: {
      test: 'no translations will default to key',
    },
  });

  let store;
  let mockRestClient;
  const configureMockStore = configureStore([thunk]);

  const actions = makeActionsOf<SelectionSummary>(EndPoints.summary);

  beforeEach(() => {
    restClientWith('someToken');
    mockRestClient = new MockAdapter(restClient);
  });

  describe('token has expired or is invalid', () => {

    it('redirect user to login page when token is invalid', async () => {
      const user: User = makeUser();
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
        mockRestClient.onGet(EndPoints.summary).reply(401, error);
        mockRestClient.onGet(EndPoints.logout).reply(204);
        return store.dispatch(fetchSummary());
      };

      await onFetchMeterSummaryWithInvalidToken();

      expect(store.getActions()).toEqual([
        actions.request(),
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
        mockRestClient.onGet(makeUrl(EndPoints.summary)).reply(401, response);
        return store.dispatch(fetchSummary());
      };

      await onFetchMeterSummaryFail();

      expect(store.getActions()).toEqual([
        actions.request(),
        actions.failure({message: 'request failed'}),
      ]);
    });

    it('fetches summary action on initial load', async () => {
      store = configureMockStore({summary: {...initialState}});

      await onFetchMeterSummary();

      expect(store.getActions()).toEqual([
        actions.request(),
        actions.success({numMeters: 2, numCities: 1, numAddresses: 2}),
      ]);
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

  describe('request timeout', () => {

    it('display error message when the request times out', async () => {
      store = configureMockStore({summary: {...initialState}});

      const fetchSummaryAndTimeout = async () => {
        const parameters: EncodedUriParameters = 'test';
        mockRestClient.onGet(makeUrl(EndPoints.summary, parameters)).timeout();
        return store.dispatch(fetchSummary(parameters));
      };

      await fetchSummaryAndTimeout();

      expect(store.getActions()).toEqual([
        actions.request(),
        actions.failure(requestTimeout()),
      ]);
    });
  });

  const onFetchMeterSummary = async (parameters?: EncodedUriParameters) => {
    const response: SelectionSummary = {numMeters: 2, numCities: 1, numAddresses: 2};
    mockRestClient.onGet(makeUrl(EndPoints.summary, parameters)).reply(200, response);
    return store.dispatch(fetchSummary(parameters));
  };
});

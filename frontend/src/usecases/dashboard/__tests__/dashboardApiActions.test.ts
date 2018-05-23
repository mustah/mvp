import MockAdapter = require('axios-mock-adapter');
import axios from 'axios';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {initTranslations} from '../../../i18n/__tests__/i18nMock';
import {EndPoints} from '../../../services/endPoints';
import {authenticate} from '../../../services/restClient';
import {makeActionsOf, RequestHandler} from '../../../state/api/apiActions';
import {fetchDashboard} from '../dashboardApiActions';
import {DashboardModel} from '../dashboardModels';
import {initialState} from '../dashboardReducer';

describe('dashboardActions', () => {

  initTranslations({
    code: 'en',
    translation: {
      'the internet connection appears to be offline': 'offline',
    },
  });

  const configureMockStore = configureStore([thunk]);
  let store;
  let mockRestClient: MockAdapter;

  const actions: RequestHandler<DashboardModel> =
    makeActionsOf<DashboardModel>(EndPoints.dashboard);

  beforeEach(() => {
    mockRestClient = new MockAdapter(axios);
    authenticate('test');
    store = configureMockStore({dashboard: initialState});
  });

  afterEach(() => {
    mockRestClient.reset();
  });

  describe('fetch successfully', () => {

    it('fetches dashboard', async () => {
      await onFetchDashboard();

      expect(store.getActions()).toEqual([
        actions.request(),
        actions.success({id: 'some-id', widgets: []}),
      ]);
    });
  });

  describe('isFetching', () => {

    it('does not fetch while fetching', async () => {
      store = configureMockStore({dashboard: {isFetching: true}});

      await onFetchDashboard();

      expect(store.getActions()).toEqual([]);
    });
  });

  describe('network error', () => {

    it('display error message when there is not internet connection', async () => {
      await fetchDashboardWhenOffline();

      expect(store.getActions()).toEqual([
        actions.request(),
        actions.failure({message: 'Offline'}),
      ]);
    });
  });

  const onFetchDashboard = async () => {
    mockRestClient.onGet(EndPoints.dashboard)
      .reply(200, {id: 'some-id', widgets: []});
    return store.dispatch(fetchDashboard());
  };

  const fetchDashboardWhenOffline = async () => {
    mockRestClient.onGet(EndPoints.dashboard).networkError();
    return store.dispatch(fetchDashboard());
  };

});

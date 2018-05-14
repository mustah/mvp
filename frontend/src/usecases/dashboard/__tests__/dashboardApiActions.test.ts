import MockAdapter = require('axios-mock-adapter');
import axios from 'axios';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {EndPoints} from '../../../services/endPoints';
import {authenticate} from '../../../services/restClient';
import {makeActionsOf, RequestHandler} from '../../../state/common/apiActions';
import {fetchDashboard} from '../dashboardApiActions';
import {DashboardModel} from '../dashboardModels';
import {initialState} from '../dashboardReducer';

describe('dashboardActions', () => {

  const configureMockStore = configureStore([thunk]);

  let mockRestClient: MockAdapter;
  let store;
  const actions: RequestHandler<DashboardModel> =
    makeActionsOf<DashboardModel>(EndPoints.dashboard);

  beforeEach(() => {
    mockRestClient = new MockAdapter(axios);
    authenticate('test');
  });

  afterEach(() => {
    mockRestClient.reset();
  });

  it('fetches dashboard', async () => {
    store = configureMockStore({dashboard: initialState});

    await onFetchDashboard();

    expect(store.getActions()).toEqual([
      actions.request(),
      actions.success({id: 'some-id', widgets: []}),
    ]);
  });

  describe('isFetching', () => {

    it('does not fetch while fetching', async () => {
      store = configureMockStore({
        dashboard: {
          initialDashboardState: initialState,
          isFetching: true,
        },
      });

      await onFetchDashboard();

      expect(store.getActions()).toEqual([]);
    });
  });

  const onFetchDashboard = async () => {
    mockRestClient.onGet().reply(200, {id: 'some-id', widgets: []});
    return store.dispatch(fetchDashboard('/id=1'));
  };

});

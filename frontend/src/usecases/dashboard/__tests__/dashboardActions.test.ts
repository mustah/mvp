import MockAdapter = require('axios-mock-adapter');
import axios from 'axios';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {Period} from '../../../components/dates/dateModels';
import {momentWithTimeZone} from '../../../helpers/dateHelpers';
import {authenticate} from '../../../services/restClient';
import {getMeterParameters, UriLookupState} from '../../../state/user-selection/userSelectionSelectors';
import {EncodedUriParameters} from '../../../types/Types';
import {DASHBOARD_SUCCESS, dashboardRequest, fetchDashboard} from '../dashboardActions';
import {initialDashboardState} from '../dashboardReducer';

describe('dashboardActions', () => {

  const configureMockStore = configureStore([thunk]);
  let mockRestClient: MockAdapter;
  let store;

  beforeEach(() => {
    store = configureMockStore(initialDashboardState);
    mockRestClient = new MockAdapter(axios);
    authenticate('test');
  });

  afterEach(() => {
    mockRestClient.reset();
  });

  it('fetches dashboard with date parameters', async () => {
    const getSelectionWithResponseOk = async () => {
      const parameters: UriLookupState = {
        userSelection: {
          selectionParameters: {
            dateRange: {period: Period.currentMonth},
          },
          isChanged: false,
          id: -1,
          name: '',
        },
        now:  momentWithTimeZone('2018-04-23T00:00:00Z').toDate(),
      };
      const encoded: EncodedUriParameters = getMeterParameters(parameters);

      mockRestClient.onGet().reply((request) => {
        return [200, request.url];
      });

      return store.dispatch(fetchDashboard(encoded));
    };

    await getSelectionWithResponseOk();

    const actions = store.getActions();
    expect(actions).toHaveLength(2);
    expect(actions[0]).toEqual(dashboardRequest());
    expect(actions[1]).toHaveProperty('type', DASHBOARD_SUCCESS);
    expect(actions[1]).toHaveProperty('payload');
    expect(actions[1].payload).toMatch(/\/dashboards\/current\?after=2[^&]+&before=2.+/);
  });

})
;

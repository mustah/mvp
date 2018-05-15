import {Medium, WidgetModel} from '../../../components/indicators/indicatorWidgetModels';
import {Status} from '../../../types/Types';
import {LOGOUT_USER} from '../../auth/authActions';
import {dashboardFailure, dashboardRequest, dashboardSuccess} from '../dashboardActions';
import {DashboardModel} from '../dashboardModels';
import {dashboard, DashboardState, initialDashboardState} from '../dashboardReducer';

describe('dashboardReducer', () => {

  it('extracts valid widgets from JSON response', () => {
    const widgets: WidgetModel[] = [
      {
        type: Medium.coldWater,
        status: Status.warning,
        total: 1000,
        pending: 20,
      },
      {
        type: Medium.current,
        status: Status.ok,
        total: 3000,
        pending: 17,
      },
      {
        type: Medium.districtHeating,
        status: Status.critical,
        total: 1000,
        pending: 122,
      },
    ];

    const capturedApiResponse: DashboardModel = {
      id: 3,
      widgets: [...widgets],
    };

    const state: DashboardState = dashboard(
      initialDashboardState,
      dashboardSuccess(capturedApiResponse),
    );

    const expected = {
      isFetching: false,
      record: {
        id: 3,
        widgets: [...widgets],
      },
    };

    expect(state).toEqual(expected);
    expect(state).not.toBe(expected);
  });

  it('is fetching when dashboard request is dispatched', () => {
    const state: DashboardState = dashboard(initialDashboardState, dashboardRequest());

    expect(state).toEqual({isFetching: true});
  });

  it('fails with error response', () => {
    const state: DashboardState = dashboard(
      initialDashboardState,
      dashboardFailure({message: 'error'}),
    );

    expect(state).toEqual({isFetching: false, error: {message: 'error'}});
  });

  describe('logout user', () => {

    it('resets state to initial state', () => {
      let state: DashboardState = dashboard(
        initialDashboardState,
        dashboardFailure({message: 'error'}),
      );

      state = dashboard(state, {type: LOGOUT_USER});

      expect(state).toEqual({isFetching: false});
    });
  });

});

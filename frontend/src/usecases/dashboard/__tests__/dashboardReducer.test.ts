import {Medium, WidgetModel} from '../../../components/indicators/indicatorWidgetModels';
import {EndPoints} from '../../../services/endPoints';
import {makeActionsOf, RequestHandler} from '../../../state/common/apiActions';
import {Status} from '../../../types/Types';
import {LOGOUT_USER} from '../../auth/authActions';
import {DashboardModel} from '../dashboardModels';
import {dashboard, DashboardState, initialState} from '../dashboardReducer';

describe('dashboardReducer', () => {

  const actions: RequestHandler<DashboardModel> =
    makeActionsOf<DashboardModel>(EndPoints.dashboard);

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
      initialState,
      actions.success(capturedApiResponse),
    );

    const expected = {
      isFetching: false,
      isSuccessfullyFetched: true,
      record: {
        id: 3,
        widgets: [...widgets],
      },
    };

    expect(state).toEqual(expected);
    expect(state).not.toBe(expected);
  });

  it('is fetching when dashboard request is dispatched', () => {
    const state: DashboardState = dashboard(initialState, actions.request());

    expect(state).toEqual({isFetching: true, isSuccessfullyFetched: false});
  });

  it('fails with error response', () => {
    const state: DashboardState = dashboard(
      initialState,
      actions.failure({message: 'error'}),
    );

    expect(state).toEqual({
      isFetching: false,
      isSuccessfullyFetched: false,
      error: {message: 'error'},
    });
  });

  describe('logout user', () => {

    it('resets state to initial state', () => {
      let state: DashboardState = dashboard(
        initialState,
        actions.failure({message: 'error'}),
      );

      state = dashboard(state, {type: LOGOUT_USER});

      expect(state).toEqual({isFetching: false, isSuccessfullyFetched: false});
    });
  });

});

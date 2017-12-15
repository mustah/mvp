import {IndicatorType, WidgetModel} from '../../../components/indicators/indicatorWidgetModels';
import {Status} from '../../../types/Types';
import {dashboardFailure, dashboardRequest, dashboardSuccess} from '../dashboardActions';
import {DashboardModel} from '../dashboardModels';
import {dashboard, DashboardState, initialState} from '../dashboardReducer';

describe('dashboardReducer', () => {

  it('returns input state when no action type is matched', () => {
    const state: DashboardState = dashboard(initialState, {type: 'UNKNOWN', payload: {}});

    expect(state).toBe(initialState);
  });

  it('extracts valid widgets from JSON response', () => {
    const widgets: WidgetModel[] = [
      {
        type: IndicatorType.coldWater,
        status: Status.warning,
        total: 1000,
        pending: 20,
      },
      {
        type: IndicatorType.current,
        status: Status.ok,
        total: 3000,
        pending: 17,
      },
      {
        type: IndicatorType.districtHeating,
        status: Status.critical,
        total: 1000,
        pending: 122,
      },
    ];

    const capturedApiResponse: DashboardModel = {
      id: 3,
      widgets: [...widgets],
    };

    const state: DashboardState = dashboard(initialState, dashboardSuccess(capturedApiResponse));

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
    const state: DashboardState = dashboard(initialState, dashboardRequest());

    expect(state).toEqual({isFetching: true});
  });

  it('fails with error response', () => {
    const state: DashboardState = dashboard(initialState, dashboardFailure({message: 'error'}));

    expect(state).toEqual({isFetching: false, error: {message: 'error'}});
  });

});

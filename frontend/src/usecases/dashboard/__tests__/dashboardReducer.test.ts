import {DASHBOARD_SUCCESS} from '../dashboardActions';
import {Status} from '../../../types/Types';
import {IndicatorType, WidgetModel} from '../../common/components/indicators/models/widgetModels';
import {dashboard, DashboardState, initialState} from '../dashboardReducer';
import {DashboardModel} from '../models/dashboardModels';

describe('Dashboard', () => {

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
      systemOverview: {
        widgets,
      },
    };

    const stateAfterReducer: DashboardState = dashboard(initialState, {
      type: DASHBOARD_SUCCESS,
      payload: capturedApiResponse,
    });

    const expected = {
      isFetching: false,
      record: {
        id: 3,
        systemOverview: {
          widgets,
        },
      },
    };

    expect(stateAfterReducer).toEqual(expected);
    expect(stateAfterReducer).not.toBe(expected);
  });

});

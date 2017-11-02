import {DASHBOARD_SUCCESS} from '../../../types/ActionTypes';
import {Status} from '../../../types/Types';
import {Indicator, IndicatorType} from '../../common/components/indicators/models/IndicatorModels';
import {dashboard, DashboardState, initialState} from '../dashboardReducer';
import {DashboardModel} from '../models/dashboardModels';

describe('Dashboard', () => {

  it('extracts valid widgets from JSON response', () => {
    const indicators: Indicator[] = [
      {
        type: IndicatorType.coldWater,
        state: Status.warning,
        subtitle: '-_-3567 punkter',
        title: 'Insamling',
        unit: '%',
        value: '95.98',
      },
      {
        type: IndicatorType.current,
        state: Status.ok,
        subtitle: '-_-3567 punkter',
        title: '-_-Insamling',
        unit: '%',
        value: '95.98',
      },
      {
        type: IndicatorType.districtHeating,
        state: Status.critical,
        subtitle: '-_-3567 punkter',
        title: '-_-Insamling',
        unit: '%',
        value: '95.98',
      },
    ];

    const capturedApiResponse: DashboardModel = {
      id: 3,
      systemOverview: {
        indicators,
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
          indicators,
        },
      },
    };

    expect(stateAfterReducer).toEqual(expected);
    expect(stateAfterReducer).not.toBe(expected);
  });

});

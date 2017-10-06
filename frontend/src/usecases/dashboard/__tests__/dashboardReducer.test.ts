import {DASHBOARD_SUCCESS} from '../../../types/ActionTypes';
import {DonutGraph} from '../../common/components/indicators/models/DonutGraphModels';
import {Indicator, IndicatorType} from '../../common/components/indicators/models/IndicatorModels';
import {dashboard, DashboardState, initialState} from '../dashboardReducer';
import {DashboardModel} from '../models/dashboardModels';

describe('Dashboard', () => {

  it('extracts valid widgets from JSON response', () => {
    const indicators: Indicator[] = [
      {
        type: IndicatorType.coldWater,
        state: 'warning',
        subtitle: '-_-3567 punkter',
        title: 'Insamling',
        unit: '%',
        value: '95.98',
      },
      {
        type: IndicatorType.current,
        state: 'ok',
        subtitle: '-_-3567 punkter',
        title: '-_-Insamling',
        unit: '%',
        value: '95.98',
      },
      {
        type: IndicatorType.districtHeating,
        state: 'critical',
        subtitle: '-_-3567 punkter',
        title: '-_-Insamling',
        unit: '%',
        value: '95.98',
      },
    ];

    const donutGraphs: DonutGraph[] = [
      {
        title: 'Tidsupplösning',
        records: [
          {name: '15m', value: 23},
          {name: '1h', value: 10},
          {name: '24h', value: 4},
        ],
      },
    ];

    const capturedApiResponse: DashboardModel = {
      id: 3,
      author: 'Sven',
      title: 'Sven dashboard from the DashboardController',
      systemOverview: {
        title: 'Sven system overview from the DashboardController',
        indicators,
        donutGraphs,
      },
    };

    const stateAfterReducer: DashboardState = dashboard(initialState, {
      type: DASHBOARD_SUCCESS,
      payload: capturedApiResponse,
    });

    const expected = {
      isFetching: false,
      record: {
        author: 'Sven',
        id: 3,
        systemOverview: {
          title: 'Sven system overview from the DashboardController',
          indicators,
          donutGraphs,
        },
        title: 'Sven dashboard from the DashboardController',
      },
    };

    expect(stateAfterReducer).toEqual(expected);
    expect(stateAfterReducer).not.toBe(expected);
  });

});

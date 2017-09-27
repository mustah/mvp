import {DASHBOARD_SUCCESS} from '../../../types/ActionTypes';
import {ColoredBoxModel} from '../../widget/models/ColoredBoxModel';
import {DonutGraphModel} from '../../widget/models/DonutGraphModel';
import {WidgetModel} from '../../widget/models/WidgetModel';
import {dashboard, DashboardState, initialState} from '../dashboardReducer';

describe('Dashboard', () => {

  it('extracts valid widgets from JSON response', () => {
    const capturedApiResponse = {
      id: 3,
      author: 'Sven',
      title: 'Sven dashboard from the DashboardController',
      systemOverview: {
        title: 'Sven system overview from the DashboardController',
        widgets: [
          {
            title: 'Insamling',
            subtitle: '-_-3567 punkter',
            state: 'warning',
            value: '95.98',
            unit: '%',
          },
          {
            title: '-_-Insamling',
            subtitle: '-_-3567 punkter',
            state: 'ok',
            value: '95.98',
            unit: '%',
          },
          {
            title: '-_-Insamling',
            subtitle: '-_-3567 punkter',
            state: 'critical',
            value: '95.98',
            unit: '%',
          },
          {
            title: 'Tidsupplösning',
            records: [
              {name: '15m', value: 23.0},
              {name: '1h', value: 10.0},
              {name: '24h', value: 4.0},
            ],
          },
        ],
      },
    };

    const stateAfterReducer: DashboardState = dashboard(initialState, {
      type: DASHBOARD_SUCCESS,
      payload: capturedApiResponse,
    });

    const expectedWidgets: WidgetModel[] = [];

    const colored1 = new ColoredBoxModel();
    colored1.state = 'warning';
    colored1.subtitle = '-_-3567 punkter';
    colored1.title = 'Insamling';
    colored1.unit = '%';
    colored1.unit = '%';
    colored1.value = '95.98';
    expectedWidgets.push(colored1);

    const colored2 = new ColoredBoxModel();
    colored2.state = 'ok';
    colored2.subtitle = '-_-3567 punkter';
    colored2.title = '-_-Insamling';
    colored2.unit = '%';
    colored2.value = '95.98';
    expectedWidgets.push(colored2);

    const colored3 = new ColoredBoxModel();
    colored3.state = 'critical';
    colored3.subtitle = '-_-3567 punkter';
    colored3.title = '-_-Insamling';
    colored3.unit = '%';
    colored3.value = '95.98';
    expectedWidgets.push(colored3);

    const donut = new DonutGraphModel();
    donut.records = [
      {name: '15m', value: 23},
      {name: '1h', value: 10},
      {name: '24h', value: 4},
    ];
    donut.title = 'Tidsupplösning';
    expectedWidgets.push(donut);

    const expected = {
      isFetching: false,
      record: {
        author: 'Sven',
        id: 3,
        systemOverview: {
          title: 'Sven system overview from the DashboardController',
          widgets: expectedWidgets,
        },
        title: 'Sven dashboard from the DashboardController',
      },
    };

    expect(stateAfterReducer).toEqual(expected);
  });

  it('throws error when the API emits an unexpected widget type', () => {
    const capturedApiResponse = {
      id: 3,
      author: 'Sven',
      title: 'Sven dashboard from the DashboardController',
      systemOverview: {
        title: 'Sven system overview from the DashboardController',
        widgets: [
          {
            title: 'Insamling',
            // no widget consists of only a 'title' attribute, this will throw an error
          },
        ],
      },
    };

    expect(() => {
      dashboard(initialState, {
        type: DASHBOARD_SUCCESS,
        payload: capturedApiResponse,
      });
    }).toThrowError('Could not instantiate a widget based on given properties');
  });
});

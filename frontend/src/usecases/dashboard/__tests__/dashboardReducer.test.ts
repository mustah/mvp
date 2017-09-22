import {dashboard, DashboardState, initialState} from '../dashboardReducer';
import {DASHBOARD_SUCCESS} from '../../../types/ActionTypes';
import {ColoredBoxModel, DonutGraphModel, WidgetModel} from '../../widget/models/WidgetModels';

it('extracts valid widgets from JSON response', () => {
  const capturedApiResponse = {
    "id": 3,
    "author": "Sven",
    "title": "Sven's dashboard from the DashboardController",
    "systemOverview": {
      "title": "Sven's system overview from the DashboardController",
      "widgets": [{
        "title": "Insamling",
        "subtitle": "-_-3567 punkter",
        "state": "warning",
        "value": "95.98",
        "unit": "%"
      }, {
        "title": "-_-Insamling",
        "subtitle": "-_-3567 punkter",
        "state": "ok",
        "value": "95.98",
        "unit": "%"
      }, {
        "title": "-_-Insamling",
        "subtitle": "-_-3567 punkter",
        "state": "critical",
        "value": "95.98",
        "unit": "%"
      }, {
        "title": "Tidsupplösning",
        "records": [{"name": "15m", "value": 23.0}, {"name": "1h", "value": 10.0}, {"name": "24h", "value": 4.0}]
      }]
    }
  };

  const stateAfterReducer: DashboardState = dashboard(initialState, {
    'type': DASHBOARD_SUCCESS,
    'payload': capturedApiResponse
  });

  const expectedWidgets: WidgetModel[] = [];

  let colored1 = new ColoredBoxModel();
  colored1.state = "warning";
  colored1.subtitle = "-_-3567 punkter";
  colored1.title = "Insamling";
  colored1.unit = "%";
  colored1.unit = "%";
  colored1.value = "95.98";
  expectedWidgets.push(colored1);

  let colored2 = new ColoredBoxModel();
  colored2.state = "ok";
  colored2.subtitle = "-_-3567 punkter";
  colored2.title = "-_-Insamling";
  colored2.unit = "%";
  colored2.value = "95.98";
  expectedWidgets.push(colored2);

  let colored3 = new ColoredBoxModel();
  colored3.state = "critical";
  colored3.subtitle = "-_-3567 punkter";
  colored3.title = "-_-Insamling";
  colored3.unit = "%";
  colored3.value = "95.98";
  expectedWidgets.push(colored3);

  let donut = new DonutGraphModel();
  donut.records = [
    {"name": "15m", "value": 23,},
    {"name": "1h", "value": 10,},
    {"name": "24h", "value": 4,},
  ];
  donut.title = "Tidsupplösning";
  expectedWidgets.push(donut);

  const expected = {
    "isFetching": false,
    "record": {
      "author": "Sven",
      "id": 3,
      "systemOverview": {
        "title": "Sven's system overview from the DashboardController",
        "widgets": expectedWidgets,
      },
      "title": "Sven's dashboard from the DashboardController",
    },
  };

  expect(stateAfterReducer).toEqual(expected);
});

it('throws error when the API emits an unexpected widget type', () => {
  const capturedApiResponse = {
    "id": 3,
    "author": "Sven",
    "title": "Sven's dashboard from the DashboardController",
    "systemOverview": {
      "title": "Sven's system overview from the DashboardController",
      "widgets": [{
        "title": "Insamling",
        // no widget consists of only a "title" attribute, this will throw an error
      }]
    }
  };

  expect(() => {
    dashboard(initialState, {
      'type': DASHBOARD_SUCCESS,
      'payload': capturedApiResponse
    });
  }).toThrowError('Could not instantiate a widget based on given properties');
});

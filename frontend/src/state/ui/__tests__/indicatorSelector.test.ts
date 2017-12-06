import {IndicatorType} from '../../../components/indicators/models/widgetModels';
import {getSelectedIndicatorReport} from '../indicator/indicatorSelectors';
import {UiState} from '../uiReducer';

describe('indicatorSelector', () => {

  it('can select the chosen indicators for the report view', () => {
    const mockedUiState: UiState = {
      tabs: {},
      indicator: {
        selectedIndicators: {
          report: IndicatorType.districtHeating,
        },
      },
      sideMenu: {
        isOpen: false,
      },
      pagination: {
        dashboard: {page: 0, limit: 0},
        collection: {page: 0, limit: 0},
        validation: {page: 0, limit: 0},
        selection: {page: 0, limit: 0},
      },
      selectionTree: {
        openListItems: [],
      },
    };
    const reportIndicators = getSelectedIndicatorReport(mockedUiState);
    expect(reportIndicators).toEqual(IndicatorType.districtHeating);
  });

  it('defaults to undefined when not having a selected indicator for the report view', () => {
    const mockedUiState: UiState = {
      tabs: {},
      indicator: {
        selectedIndicators: {},
      },
      sideMenu: {
        isOpen: false,
      },
      pagination: {
        dashboard: {page: 0, limit: 0},
        collection: {page: 0, limit: 0},
        validation: {page: 0, limit: 0},
        selection: {page: 0, limit: 0},
      },
      selectionTree: {
        openListItems: [],
      },
    };
    const reportIndicators = getSelectedIndicatorReport(mockedUiState);
    expect(reportIndicators).toEqual(undefined);
  });

});

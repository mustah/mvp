import {IndicatorType} from '../../../components/indicators/indicatorWidgetModels';
import {Maybe} from '../../../helpers/Maybe';
import {getSelectedIndicatorTypeForReport} from '../indicator/indicatorSelectors';
import {UiState} from '../uiReducer';

describe('indicatorSelector', () => {

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

  it('can select the chosen indicators for the report view', () => {
    const indicatorType = getSelectedIndicatorTypeForReport({...mockedUiState}).get();

    expect(indicatorType).toEqual(IndicatorType.districtHeating);
  });

  it('defaults to undefined when not having a selected indicator for the report view', () => {
    const state: UiState = {
      ...mockedUiState,
      indicator: {
        selectedIndicators: {},
      },
    };

    const indicatorType: Maybe<IndicatorType> = getSelectedIndicatorTypeForReport(state);

    expect(indicatorType.isNothing()).toBe(true);
  });

});

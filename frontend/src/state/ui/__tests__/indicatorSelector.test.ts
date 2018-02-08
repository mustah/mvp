import {IndicatorType} from '../../../components/indicators/indicatorWidgetModels';
import {getSelectedIndicatorTypeForReport} from '../indicator/indicatorSelectors';
import {initialPaginationState} from '../pagination/paginationReducer';
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
    pagination: initialPaginationState,
    selectionTree: {
      openListItems: [],
    },
    message: {
      isOpen: false,
      message: '',
    },
  };

  it('can select the chosen indicators for the report view', () => {
    const indicatorType = getSelectedIndicatorTypeForReport({...mockedUiState});

    expect(indicatorType).toEqual(IndicatorType.districtHeating);
  });

  it('defaults to districtHeating when not having a selected indicator for the report view', () => {
    const state: UiState = {
      ...mockedUiState,
      indicator: {
        selectedIndicators: {},
      },
    };

    const indicatorType = getSelectedIndicatorTypeForReport(state);

    expect(indicatorType).toBe(IndicatorType.districtHeating);
  });

});

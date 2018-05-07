import {IndicatorType} from '../../../components/indicators/indicatorWidgetModels';
import {toggleIndicatorWidget} from '../indicator/indicatorActions';
import {indicator, IndicatorState, initialState} from '../indicator/indicatorReducer';

describe('indicatorReducer', () => {

  it('can select an indicator', () => {
    const state: IndicatorState = indicator(initialState, toggleIndicatorWidget(['report', IndicatorType.gas]));

    const expected: IndicatorState = {
      selectedIndicators: {
        report: [IndicatorType.gas],
      },
    };

    expect(state).toEqual(expected);
  });

  it('can have multiple indicators selected at the same time', () => {
    const oneSelectedIndicator: IndicatorState =
      indicator(initialState, toggleIndicatorWidget(['report', IndicatorType.districtHeating]));

    const expectedOne: IndicatorState = {
      selectedIndicators: {
        report: [
          IndicatorType.districtHeating,
        ],
      },
    };

    expect(oneSelectedIndicator).toEqual(expectedOne);

    const twoSelectedIndicators: IndicatorState =
      indicator(oneSelectedIndicator, toggleIndicatorWidget(['report', IndicatorType.gas]));

    const expectedTwo: IndicatorState = {
      selectedIndicators: {
        report: [
          IndicatorType.districtHeating,
          IndicatorType.gas,
        ],
      },
    };

    expect(twoSelectedIndicators).toEqual(expectedTwo);
  });

  it('can deselect indicators', () => {
    const selected: IndicatorState =
      indicator(initialState, toggleIndicatorWidget(['report', IndicatorType.districtHeating]));

    const expectedSelected: IndicatorState = {
      selectedIndicators: {
        report: [IndicatorType.districtHeating],
      },
    };

    expect(selected).toEqual(expectedSelected);

    const deselected: IndicatorState =
      indicator(selected, toggleIndicatorWidget(['report', IndicatorType.districtHeating]));

    const expectedDeselected: IndicatorState = {
      selectedIndicators: {
        report: [],
      },
    };

    expect(deselected).toEqual(expectedDeselected);
  });
});

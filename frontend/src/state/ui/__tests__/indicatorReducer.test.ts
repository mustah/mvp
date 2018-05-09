import {Medium} from '../../../components/indicators/indicatorWidgetModels';
import {toggleIndicatorWidget} from '../indicator/indicatorActions';
import {indicator, IndicatorState, initialState} from '../indicator/indicatorReducer';

describe('indicatorReducer', () => {

  it('can select an indicator', () => {
    const state: IndicatorState = indicator(initialState, toggleIndicatorWidget(['report', Medium.gas]));

    const expected: IndicatorState = {
      selectedIndicators: {
        report: [Medium.gas],
      },
    };

    expect(state).toEqual(expected);
  });

  it('can have multiple indicators selected at the same time', () => {
    const oneSelectedIndicator: IndicatorState =
      indicator(initialState, toggleIndicatorWidget(['report', Medium.districtHeating]));

    const expectedOne: IndicatorState = {
      selectedIndicators: {
        report: [
          Medium.districtHeating,
        ],
      },
    };

    expect(oneSelectedIndicator).toEqual(expectedOne);

    const twoSelectedIndicators: IndicatorState =
      indicator(oneSelectedIndicator, toggleIndicatorWidget(['report', Medium.gas]));

    const expectedTwo: IndicatorState = {
      selectedIndicators: {
        report: [
          Medium.districtHeating,
          Medium.gas,
        ],
      },
    };

    expect(twoSelectedIndicators).toEqual(expectedTwo);
  });

  it('can deselect indicators', () => {
    const selected: IndicatorState =
      indicator(initialState, toggleIndicatorWidget(['report', Medium.districtHeating]));

    const expectedSelected: IndicatorState = {
      selectedIndicators: {
        report: [Medium.districtHeating],
      },
    };

    expect(selected).toEqual(expectedSelected);

    const deselected: IndicatorState =
      indicator(selected, toggleIndicatorWidget(['report', Medium.districtHeating]));

    const expectedDeselected: IndicatorState = {
      selectedIndicators: {
        report: [],
      },
    };

    expect(deselected).toEqual(expectedDeselected);
  });

});

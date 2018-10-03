import {Medium} from '../../../../components/indicators/indicatorWidgetModels';
import {setSelectedEntries} from '../../../../usecases/report/reportActions';
import {Quantity} from '../../graph/measurement/measurementModels';
import {indicator, IndicatorState, initialState} from '../indicatorReducer';

describe('indicatorReducer', () => {

  it('deselects selected indicators and quantities when the last report item is deselected', () => {
    const nonEmptyState: IndicatorState = {
      ...initialState,
      selectedIndicators: {
        report: [Medium.districtHeating],
      },
      selectedQuantities: [Quantity.volume],
    };

    const stateAfterDeselection = indicator(nonEmptyState, setSelectedEntries({
      ids: [],
      indicatorsToSelect: [],
      quantitiesToSelect: [],
    }));

    expect(stateAfterDeselection).toEqual(initialState);
  });

  it('selects indicator and quantity on demand', () => {
    const stateAfterFirstReportItemIsSelected: IndicatorState = indicator(
      initialState,
      setSelectedEntries({
        ids: ['123'],
        indicatorsToSelect: [Medium.roomSensor],
        quantitiesToSelect: [Quantity.externalTemperature],
      }),
    );

    const expected: IndicatorState = {
      ...initialState,
      selectedIndicators: {
        report: [Medium.roomSensor],
      },
      selectedQuantities: [Quantity.externalTemperature],
    };

    expect(stateAfterFirstReportItemIsSelected).toEqual(expected);
  });

});

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

    const stateAfterDeselection = indicator(nonEmptyState, setSelectedEntries([]));

    expect(stateAfterDeselection).toEqual(initialState);
  });

});

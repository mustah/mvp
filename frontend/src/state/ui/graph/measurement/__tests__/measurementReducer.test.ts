import {selectQuantities} from '../measurementActions';
import {initialMeasurementState, measurements} from '../measurementReducer';

describe('measurementReducer', () => {

  it('sets quantites', () => {
    expect(measurements(initialMeasurementState, selectQuantities(['Power', 'Temperature'])))
      .toEqual({
        ...initialMeasurementState,
        selectedQuantities: ['Power', 'Temperature'],
      });
  });

});

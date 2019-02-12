import {EXPORT_TO_EXCEL, EXPORT_TO_EXCEL_SUCCESS} from '../measurementActions';
import {MeasurementState} from '../measurementModels';
import {initialState, measurement} from '../measurementReducer';

describe('measurementReducer', () => {

  it('listens to EXPORT_TO_EXCEL', () => {
    const actual: MeasurementState = measurement(initialState, {type: EXPORT_TO_EXCEL});
    const expected: MeasurementState = {
      ...initialState,
      isExportingToExcel: true,
    };
    expect(actual).toEqual(expected);
  });

  describe('listens to EXPORT_TO_EXCEL_SUCCESS', () => {
    const actual: MeasurementState = measurement(
      {...initialState, isExportingToExcel: true},
      {type: EXPORT_TO_EXCEL_SUCCESS}
    );
    const expected: MeasurementState = {
      ...initialState,
      isExportingToExcel: false,
    };
    expect(actual).toEqual(expected);
  });

});

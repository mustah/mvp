import {exportToExcelAction, exportToExcelSuccess} from '../measurementActions';
import {MeasurementState} from '../measurementModels';
import {initialState, measurement} from '../measurementReducer';

describe('measurementReducer', () => {

  describe('export to excel', () => {

    it('sets that it is exporting to excel', () => {
      const actual: MeasurementState = measurement(initialState, exportToExcelAction());

      const expected: MeasurementState = {...initialState, isExportingToExcel: true};
      expect(actual).toEqual(expected);
    });

    it('resets isExportingToExcel property', () => {
      const actual: MeasurementState = measurement({...initialState, isExportingToExcel: true}, exportToExcelSuccess());

      const expected: MeasurementState = {...initialState, isExportingToExcel: false};
      expect(actual).toEqual(expected);
    });
  });

});

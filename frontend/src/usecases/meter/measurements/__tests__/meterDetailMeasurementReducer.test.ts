import {MeasurementState} from '../../../../state/ui/graph/measurement/measurementModels';
import {initialState} from '../../../../state/ui/graph/measurement/measurementReducer';
import {meterDetailExportToExcelAction, meterDetailExportToExcelSuccess} from '../meterDetailMeasurementActions';
import {meterDetailMeasurement} from '../meterDetailMeasurementReducer';

describe('meterDetailMeasurementReducer', () => {

  describe('export to excel', () => {

    it('sets that it is exporting to excel', () => {
      const actual: MeasurementState = meterDetailMeasurement(initialState, meterDetailExportToExcelAction());

      const expected: MeasurementState = {...initialState, isExportingToExcel: true};
      expect(actual).toEqual(expected);
    });

    it('resets isExportingToExcel property', () => {
      const actual: MeasurementState = meterDetailMeasurement(
        {...initialState, isExportingToExcel: true},
        meterDetailExportToExcelSuccess(),
      );

      const expected: MeasurementState = {...initialState, isExportingToExcel: false};
      expect(actual).toEqual(expected);
    });
  });

});

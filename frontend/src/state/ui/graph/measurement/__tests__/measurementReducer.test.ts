import {Maybe} from '../../../../../helpers/Maybe';
import {Action, ErrorResponse} from '../../../../../types/Types';
import {ReportSector} from '../../../../report/reportModels';
import {exportToExcelAction, exportToExcelSuccess} from '../measurementActions';
import {MeasurementResponse, MeasurementState} from '../measurementModels';
import {initialState, measurement} from '../measurementReducer';

describe('measurementReducer', () => {

  describe('export to excel', () => {

    it('sets that it is exporting to excel', () => {

      const actual: MeasurementState = measurement(
        initialState,
        exportToExcelAction(ReportSector.report)() as Action<MeasurementResponse>
      );

      const expected: MeasurementState = {...initialState, isExportingToExcel: true};
      expect(actual).toEqual(expected);
    });

    it('resets isExportingToExcel property', () => {
      const actual: MeasurementState =
        measurement(
          {...initialState, isExportingToExcel: true},
          exportToExcelSuccess(ReportSector.report)() as Action<Maybe<ErrorResponse>>
        );

      const expected: MeasurementState = {...initialState, isExportingToExcel: false};
      expect(actual).toEqual(expected);
    });
  });

});

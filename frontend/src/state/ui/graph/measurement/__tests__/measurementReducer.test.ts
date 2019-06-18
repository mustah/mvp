import {Period, TemporalResolution} from '../../../../../components/dates/dateModels';
import {Maybe} from '../../../../../helpers/Maybe';
import {Action, ErrorResponse} from '../../../../../types/Types';
import {selectResolution, setReportTimePeriod} from '../../../../report/reportActions';
import {ReportSector} from '../../../../report/reportModels';
import {search} from '../../../../search/searchActions';
import {makeMeterQuery} from '../../../../search/searchModels';
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

  describe('actions will reset reducer to perform new measurements search', () => {
    const sector = ReportSector.report;
    const prevState: MeasurementState = {...initialState, isExportingToExcel: true};

    it('resets when period is changed', () => {
      const state = measurement(prevState, setReportTimePeriod(sector)({period: Period.yesterday}));

      expect(state).toBe(initialState);
    });

    it('resets when resolution is changed', () => {
      const state = measurement(prevState, selectResolution(sector)(TemporalResolution.month));

      expect(state).toBe(initialState);
    });

    it('resets when a new global search is made', () => {
      const state = measurement(prevState, search(makeMeterQuery('ELV')));

      expect(state).toBe(initialState);
    });

  });

});

import {toLocation} from '../../../../__tests__/testDataFactory';
import {routes} from '../../../../app/routes';
import {Period, TemporalResolution} from '../../../../components/dates/dateModels';
import {locationChange} from '../../../../state/location/locationActions';
import {MeasurementState} from '../../../../state/ui/graph/measurement/measurementModels';
import {initialState} from '../../../../state/ui/graph/measurement/measurementReducer';
import {
  meterDetailExportToExcelAction,
  meterDetailExportToExcelSuccess,
  selectResolution,
  setTimePeriod
} from '../meterDetailMeasurementActions';
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

  describe('location change', () => {

    it('resets state when user navigates to meter details page', () => {
      const state: MeasurementState = {...initialState, isFetching: true};

      const newState: MeasurementState = meterDetailMeasurement(
        state,
        locationChange(toLocation(`${routes.meter}/${'123'}`))
      );

      expect(newState).toBe(initialState);
    });

    it('will keep the last state when user navigates somewhere else than meter details page', () => {
      const state: MeasurementState = {...initialState, isFetching: true};

      const newState: MeasurementState = meterDetailMeasurement(
        state,
        locationChange(toLocation(routes.dashboard))
      );

      expect(newState).toBe(state);
    });
  });

  describe('change period', () => {

    it('will reset state', () => {
      const newState: MeasurementState = meterDetailMeasurement(
        {...initialState, isFetching: true},
        setTimePeriod({period: Period.previousMonth})
      );

      expect(newState).toBe(initialState);
    });
  });

  describe('change resolution', () => {

    it('will reset state', () => {
      const newState: MeasurementState = meterDetailMeasurement(
        {...initialState, isFetching: true},
        selectResolution(TemporalResolution.month)
      );

      expect(newState).toBe(initialState);
    });
  });

});

import {createAction, createStandardAction} from 'typesafe-actions';
import {TemporalResolution} from '../../../components/dates/dateModels';
import {Maybe} from '../../../helpers/Maybe';
import {GetState} from '../../../reducers/rootReducer';
import {MeasurementResponse} from '../../../state/ui/graph/measurement/measurementModels';
import {SelectionInterval} from '../../../state/user-selection/userSelectionModels';
import {ErrorResponse} from '../../../types/Types';

export const meterDetailMeasurementRequest = createAction('METER_DETAIL_MEASUREMENT_REQUEST');

export const meterDetailMeasurementSuccess =
  createStandardAction('METER_DETAIL_MEASUREMENT_SUCCESS')<MeasurementResponse>();

export const meterDetailMeasurementFailure =
  createStandardAction('METER_DETAIL_MEASUREMENT_FAILURE')<Maybe<ErrorResponse>>();

export const meterDetailExportToExcelAction = createAction('METER_DETAIL_EXPORT_TO_EXCEL');
export const meterDetailExportToExcelSuccess = createAction('METER_DETAIL_EXPORT_TO_EXCEL_SUCCESS');

export const exportToExcel = () =>
  (dispatch, getState: GetState) => {
    if (!getState().domainModels.meterDetailMeasurement.isExportingToExcel) {
      dispatch(meterDetailExportToExcelAction());
    }
  };

export const setTimePeriod = createStandardAction('METER_DETAILS_SET_TIME_PERIOD')<SelectionInterval>();
export const selectResolution = createStandardAction(`METER_DETAILS_SELECT_RESOLUTION`)<TemporalResolution>();

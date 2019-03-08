import {createAction, createStandardAction} from 'typesafe-actions';
import {Maybe} from '../../../helpers/Maybe';
import {GetState} from '../../../reducers/rootReducer';
import {MeasurementResponse} from '../../../state/ui/graph/measurement/measurementModels';
import {ErrorResponse} from '../../../types/Types';

export const meterDetailMeasurementRequest = createAction('METER_DETAIL_MEASUREMENT_REQUEST');

export const meterDetailMeasurementSuccess =
  createStandardAction('METER_DETAIL_MEASUREMENT_SUCCESS')<MeasurementResponse>();

export const meterDetailMeasurementFailure =
  createStandardAction('METER_DETAIL_MEASUREMENT_FAILURE')<Maybe<ErrorResponse>>();

export const meterDetailExportToExcelAction = createAction('METER_DETAIL_EXPORT_TO_EXCEL');

// TODO: Daniel, make sure that is called after an export - this resets that it can export again.
export const meterDetailExportToExcelSuccess = createAction('METER_DETAIL_EXPORT_TO_EXCEL_SUCCESS');

export const exportToExcel = () =>
  (dispatch, getState: GetState) => {
    if (!getState().domainModels.meterDetailMeasurement.isExportingToExcel) {
      dispatch(meterDetailExportToExcelAction());
    }
  };

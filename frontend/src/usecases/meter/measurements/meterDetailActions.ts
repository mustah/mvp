import {createStandardAction} from 'typesafe-actions';
import {GetState} from '../../../reducers/rootReducer';
import {
  meterDetailExportToExcelAction
} from '../../../state/ui/graph/measurement/measurementActions';
import {SelectionInterval} from '../../../state/user-selection/userSelectionModels';

export const setMeterDetailsTimePeriod = createStandardAction('SET_METER_DETAILS_TIME_PERIOD')<SelectionInterval>();

export const exportToExcel = () =>
  (dispatch, getState: GetState) => {
    if (!getState().domainModels.meterDetailMeasurement.isExportingToExcel) {
      dispatch(meterDetailExportToExcelAction());
    }
  };

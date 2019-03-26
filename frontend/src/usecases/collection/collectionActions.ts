import {createAction, createStandardAction} from 'typesafe-actions';
import {GetState} from '../../reducers/rootReducer';
import {SelectionInterval} from '../../state/user-selection/userSelectionModels';

export const setCollectionTimePeriod = createStandardAction('SET_COLLECTION_TIME_PERIOD')<SelectionInterval>();

export const exportToExcelAction = createAction('COLLECTION_STATS_EXPORT_TO_EXCEL');
export const exportToExcelSuccess = createAction('COLLECTION_STATS_EXPORT_TO_EXCEL_SUCCESS');

export const exportToExcel = () =>
  (dispatch, getState: GetState) => {
    if (!getState().collection.isExportingToExcel) {
      dispatch(exportToExcelAction());
    }
  };

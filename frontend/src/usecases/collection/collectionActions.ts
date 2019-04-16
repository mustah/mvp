import {createAction, createStandardAction} from 'typesafe-actions';
import {GetState} from '../../reducers/rootReducer';
import {SelectionInterval} from '../../state/user-selection/userSelectionModels';
import {ModelSectors} from '../../types/Types';

export const setCollectionTimePeriod = (sector: ModelSectors) =>
  createStandardAction(`SET_COLLECTION_TIME_PERIOD_${sector}`)<SelectionInterval>();

export const exportToExcelAction = (sector: ModelSectors) =>
  createAction(`COLLECTION_STATS_EXPORT_TO_EXCEL_${sector}`);
export const exportToExcelSuccess = (sector: ModelSectors) =>
  createAction(`COLLECTION_STATS_EXPORT_TO_EXCEL_SUCCESS_${sector}`);

export const exportToExcel = () =>
  (dispatch, getState: GetState) => {
    if (!getState().collection.isExportingToExcel) {
      dispatch(exportToExcelAction(ModelSectors.collection)());
    }
  };

export const meterCollectionExportToExcel = () =>
  (dispatch, getState: GetState) => {
    if (!getState().meterCollection.isExportingToExcel) {
      dispatch(exportToExcelAction(ModelSectors.meterCollection)());
    }
  };

import {createAction, createStandardAction} from 'typesafe-actions';
import {GetState} from '../../reducers/rootReducer';
import {changeCollectionToolbarView} from '../../state/ui/toolbar/toolbarActions';
import {SelectionInterval} from '../../state/user-selection/userSelectionModels';
import {Sectors} from '../../types/Types';

export const setCollectionStatsTimePeriod =
  createStandardAction(`SET_COLLECTION_STATS_TIME_PERIOD`)<SelectionInterval>();

export const setMeterCollectionStatsTimePeriod =
  createStandardAction(`SET_METER_COLLECTION_STATS_TIME_PERIOD`)<SelectionInterval>();

export const collectionStatsExportToExcel = createAction(`COLLECTION_STATS_EXPORT_TO_EXCEL`);
export const meterCollectionStatsExportToExcel = createAction(`METER_COLLECTION_STATS_EXPORT_TO_EXCEL`);

export const collectionStatsExportToExcelSuccess = createAction(`COLLECTION_STATS_EXPORT_TO_EXCEL_SUCCESS`);
export const meterCollectionStatsExportToExcelSuccess = createAction(`METER_COLLECTION_STATS_EXPORT_TO_EXCEL_SUCCESS`);

export const exportCollectionStatsToExcel = () =>
  (dispatch, getState: GetState) => {
    if (!getState().collection.isExportingToExcel) {
      dispatch(collectionStatsExportToExcel());
    }
  };

export const exportMeterCollectionStatsToExcel = () =>
  (dispatch, getState: GetState) => {
    if (!getState().meterCollection.isExportingToExcel) {
      dispatch(meterCollectionStatsExportToExcel());
    }
  };

export const changeToolbarView = changeCollectionToolbarView(Sectors.collection);

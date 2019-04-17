import {createAction, createStandardAction} from 'typesafe-actions';
import {GetState} from '../../reducers/rootReducer';
import {changeCollectionToolbarView} from '../../state/ui/toolbar/toolbarActions';
import {SelectionInterval} from '../../state/user-selection/userSelectionModels';
import {Sectors} from '../../types/Types';

export const setCollectionTimePeriod = (sector: Sectors) =>
  createStandardAction(`SET_COLLECTION_TIME_PERIOD_${sector}`)<SelectionInterval>();

export const exportToExcelAction = (sector: Sectors) =>
  createAction(`COLLECTION_STATS_EXPORT_TO_EXCEL_${sector}`);

export const exportToExcelSuccess = (sector: Sectors) =>
  createAction(`COLLECTION_STATS_EXPORT_TO_EXCEL_SUCCESS_${sector}`);

export const exportToExcel = () =>
  (dispatch, getState: GetState) => {
    if (!getState().collection.isExportingToExcel) {
      dispatch(exportToExcelAction(Sectors.collection)());
    }
  };

export const changeToolbarView = changeCollectionToolbarView(Sectors.collection);

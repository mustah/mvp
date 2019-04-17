import {GetState} from '../../../reducers/rootReducer';
import {Sectors} from '../../../types/Types';
import {exportToExcelAction} from '../../collection/collectionActions';

export const exportToExcel = () =>
  (dispatch, getState: GetState) => {
    if (!getState().meterCollection.isExportingToExcel) {
      dispatch(exportToExcelAction(Sectors.meterCollection)());
    }
  };

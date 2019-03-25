import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {MeasurementState} from '../../../../state/ui/graph/measurement/measurementModels';
import {initialState} from '../../../../state/ui/graph/measurement/measurementReducer';
import {AuthState} from '../../../auth/authModels';
import {exportToExcel, meterDetailExportToExcelAction} from '../meterDetailMeasurementActions';

describe('meterDetailMeasurementAction', () => {

  const configureMockStore = configureStore([thunk]);

  const storeWith = (meterDetailMeasurement: MeasurementState, auth?: AuthState) =>
    configureMockStore({
      domainModels: {meterDetailMeasurement},
      measurement: initialState,
      auth
    });

  describe('exportToExcel', () => {

    it('dispatches action if no export is ongoing', () => {
      const store = storeWith(initialState);

      store.dispatch(exportToExcel());

      expect(store.getActions()).toEqual([meterDetailExportToExcelAction()]);
    });

    it('does not dispatch action if export is ongoing', () => {
      const store = storeWith({
        ...initialState,
        isExportingToExcel: true,
      });

      store.dispatch(exportToExcel());

      expect(store.getActions()).toEqual([]);
    });
  });

});

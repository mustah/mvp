import axios from 'axios';
import {routerActions} from 'react-router-redux';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {testData} from '../../../../__tests__/TestDataFactory';
import {makeRestClient} from '../../../../services/restClient';
import {IdNamed, Period} from '../../../../types/Types';
import {meterRequest} from '../../../domain-models/meter/meterActions';
import {
  closeSearch,
  closeSelectionPage,
  deselectSelection,
  selectPeriod,
  selectPeriodAction,
  setSelection,
  toggleSelection,
} from '../selectionActions';
import {parameterNames, SelectionParameter, SelectionState} from '../selectionModels';
import {initialState, selection} from '../selectionReducer';
import MockAdapter = require('axios-mock-adapter');

const configureMockStore = configureStore([thunk]);

describe('selectionActions', () => {

  const gothenburg = {...testData.geoData.cities[0]};
  const stockholm = {...testData.geoData.cities[1]};

  let mockRestClient;
  let store;

  beforeEach(() => {
    store = configureMockStore({});
    mockRestClient = new MockAdapter(axios);
    makeRestClient('test');
  });

  afterEach(() => {
    mockRestClient.reset();
  });

  describe('close selection page', () => {

    it('closes selection page and navigates back to previous page', () => {
      store.dispatch(closeSearch());

      expect(store.getActions()).toEqual([
        closeSelectionPage(),
        routerActions.goBack(),
      ]);
    });
  });

  describe('toggle selection', () => {

    it('set selection', async () => {
      store = configureMockStore({searchParameters: {selection: {...initialState}}});

      const selection: IdNamed = {...gothenburg};

      const parameter: SelectionParameter = {...selection, parameter: parameterNames.cities};

      store.dispatch(toggleSelection(parameter));

      expect(store.getActions()).toEqual([
        setSelection(parameter),
        meterRequest(),
      ]);
    });

    it('select period', async () => {
      store = configureMockStore({searchParameters: {selection: {...initialState}}});

      const period = Period.previousMonth;

      store.dispatch(selectPeriod(period));

      expect(store.getActions()).toEqual([
        selectPeriodAction(period),
        meterRequest(),
      ]);
    });

    it('deselects selected city', () => {
      const payload: SelectionParameter = {...stockholm, parameter: parameterNames.cities};
      const state: SelectionState = selection(initialState, setSelection(payload));

      store = configureMockStore({searchParameters: {selection: state}});

      const parameter: SelectionParameter = {...stockholm, parameter: parameterNames.cities};

      store.dispatch(toggleSelection(parameter));

      expect(store.getActions()).toEqual([
        deselectSelection(parameter),
        meterRequest(),
      ]);
    });

    it('set several selections', async () => {
      store = configureMockStore({searchParameters: {selection: initialState}});

      const p1: SelectionParameter = {...stockholm, parameter: parameterNames.cities};
      const p2: SelectionParameter = {...gothenburg, parameter: parameterNames.cities};

      store.dispatch(toggleSelection(p1));
      store.dispatch(toggleSelection(p2));

      expect(store.getActions()).toEqual([
        setSelection(p1),
        meterRequest(),
        setSelection(p2),
        meterRequest(),
      ]);
    });
  });

});

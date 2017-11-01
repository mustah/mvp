import axios from 'axios';
import {normalize} from 'normalizr';
import {routerActions} from 'react-router-redux';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {makeRestClient} from '../../../../services/restClient';
import {IdNamed, Period} from '../../../../types/Types';
import {meterRequest} from '../../../domain-models/meter/meterActions';
import {
  closeSearch,
  closeSelectionPage,
  deselectSelection,
  fetchSelections,
  selectionFailure,
  selectionRequest,
  selectionSuccess,
  selectPeriod,
  selectPeriodAction,
  setSelection,
  toggleSelection,
} from '../selectionActions';
import {parameterNames, SelectionParameter} from '../selectionModels';
import {addCityEntity, initialState, selection, SelectionState} from '../selectionReducer';
import {selectionSchema} from '../selectionSchemas';
import MockAdapter = require('axios-mock-adapter');

const dbJsonData = require('./../../../../../mockdata');
const mockData = dbJsonData();
const configureMockStore = configureStore([thunk]);

describe('selectionActions', () => {

  const gothenburg = {...mockData.selections.cities[0]};
  const stockholm = {...mockData.selections.cities[1]};

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

  describe('fetch selections', () => {

    it('normalizes data', async () => {
      await fetchFakeSelections();

      expect(store.getActions()).toEqual([
        selectionRequest(),
        selectionSuccess(normalize(mockData.selections, selectionSchema)),
      ]);
    });

    it('throws exception when no data exists', async () => {
      const data = {message: 'failed'};

      mockRestClient.onGet('/selections').reply(401, data);

      await store.dispatch(fetchSelections());

      expect(store.getActions()).toEqual([
        selectionRequest(),
        selectionFailure({...data}),
      ]);
    });
  });

  describe('toggle selection', () => {

    it('set selection', async () => {
      store = configureMockStore({searchParameters: {selection: {...initialState}}});

      const selection: IdNamed = mockData.selections.cities[0];

      const parameter: SelectionParameter = {...selection, parameter: parameterNames.cities};

      store.dispatch(toggleSelection(parameter));

      expect(store.getActions()).toEqual([
        setSelection(parameter),
        meterRequest(),
      ]);
    });

    it('select period', async () => {
      store = configureMockStore({searchParameters: {selection: {...initialState}}});

      const period = Period.now;

      store.dispatch(selectPeriod(period));

      expect(store.getActions()).toEqual([
        selectPeriodAction(period),
        meterRequest(),
      ]);
    });

    it('deselects selected city', () => {
      const prevState: SelectionState = addCityEntity(initialState, {...stockholm});
      const payload: SelectionParameter = {...stockholm, parameter: parameterNames.cities};
      const state: SelectionState = selection(prevState, setSelection(payload));

      store = configureMockStore({searchParameters: {selection: state}});

      const parameter: SelectionParameter = {...stockholm, parameter: parameterNames.cities};

      store.dispatch(toggleSelection(parameter));

      expect(store.getActions()).toEqual([
        deselectSelection(parameter),
        meterRequest(),
      ]);
    });

    it('set several selections', async () => {
      const state: SelectionState = addCityEntity(initialState, {...stockholm, ...gothenburg});

      store = configureMockStore({searchParameters: {selection: state}});

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

  const fetchFakeSelections = async () => {
    mockRestClient.onGet('/selections').reply(200, mockData.selections);

    return store.dispatch(fetchSelections());
  };

});

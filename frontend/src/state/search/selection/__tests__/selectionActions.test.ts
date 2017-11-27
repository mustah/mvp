import axios from 'axios';
import {routerActions} from 'react-router-redux';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {testData} from '../../../../__tests__/TestDataFactory';
import {makeRestClient} from '../../../../services/restClient';
import {makeUrl} from '../../../../services/urlFactory';
import {IdNamed, Period} from '../../../../types/Types';
import {gatewayRequest, meterRequest} from '../../../domain-models/domainModelsActions';
import {SearchParameterState} from '../../searchParameterReducer';
import {
  closeSelectionPage,
  closeSelectionPageAction,
  deselectSelection,
  selectPeriod,
  selectPeriodAction,
  selectSavedSelection,
  selectSavedSelectionAction,
  setSelection,
  toggleSelection,
} from '../selectionActions';
import {ParameterName, SelectionParameter, SelectionState} from '../selectionModels';
import {initialState, selection} from '../selectionReducer';
import {getEncodedUriParametersForMeters} from '../selectionSelectors';
import MockAdapter = require('axios-mock-adapter');

const configureMockStore = configureStore([thunk]);

describe('selectionActions', () => {

  const gothenburg = {...testData.selections.cities[0]};
  const stockholm = {...testData.selections.cities[1]};

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
      store.dispatch(closeSelectionPage());

      expect(store.getActions()).toEqual([
        closeSelectionPageAction(),
        routerActions.goBack(),
      ]);
    });
  });

  describe('toggle selection', () => {

    it('set selection', async () => {
      const rootState = {searchParameters: {selection: {...initialState}, saved: []}};
      onFakeFetchMetersAndGateways(rootState.searchParameters);
      store = configureMockStore(rootState);

      const selection: IdNamed = {...gothenburg};

      const parameter: SelectionParameter = {...selection, parameter: ParameterName.cities};

      store.dispatch(toggleSelection(parameter));

      expect(store.getActions()).toEqual([
        setSelection(parameter),
        meterRequest.request(),
        gatewayRequest.request(),
      ]);
    });

    it('select period', async () => {
      const rootState = {searchParameters: {selection: {...initialState}, saved: []}};
      onFakeFetchMetersAndGateways(rootState.searchParameters);
      store = configureMockStore(rootState);

      const period = Period.previousMonth;

      store.dispatch(selectPeriod(period));

      expect(store.getActions()).toEqual([
        selectPeriodAction(period),
        meterRequest.request(),
        gatewayRequest.request(),
      ]);
    });

    it('deselects selected city', () => {
      const payload: SelectionParameter = {...stockholm, parameter: ParameterName.cities};
      const state: SelectionState = selection(initialState, setSelection(payload));

      const rootState = {searchParameters: {selection: state, saved: []}};
      onFakeFetchMetersAndGateways(rootState.searchParameters);
      store = configureMockStore(rootState);

      const parameter: SelectionParameter = {...stockholm, parameter: ParameterName.cities};

      store.dispatch(toggleSelection(parameter));

      expect(store.getActions()).toEqual([
        deselectSelection(parameter),
        meterRequest.request(),
        gatewayRequest.request(),
      ]);
    });

    it('set several selections', () => {
      const rootState = {searchParameters: {selection: initialState, saved: []}};
      onFakeFetchMetersAndGateways(rootState.searchParameters);
      store = configureMockStore(rootState);

      const p1: SelectionParameter = {...stockholm, parameter: ParameterName.cities};
      const p2: SelectionParameter = {...gothenburg, parameter: ParameterName.cities};

      store.dispatch(toggleSelection(p1));
      store.dispatch(toggleSelection(p2));

      expect(store.getActions()).toEqual([
        setSelection(p1),
        meterRequest.request(),
        gatewayRequest.request(),
        setSelection(p2),
        meterRequest.request(),
        gatewayRequest.request(),
      ]);
    });
  });

  describe('select from saved selections', () => {

    it('sets new selection', () => {
      const savedSelection21 = {
        ...initialState,
        id: 21,
        name: 'test 21',
      };

      const saved: SelectionState[] = [
        {
          ...initialState,
          id: 1,
          name: 'test 1',
        },
        savedSelection21,
      ];

      const rootState = {searchParameters: {selection: initialState, saved}};
      onFakeFetchMetersAndGateways(rootState.searchParameters);
      store = configureMockStore(rootState);

      store.dispatch(selectSavedSelection(savedSelection21.id));

      expect(store.getActions()).toEqual([
        selectSavedSelectionAction(savedSelection21),
        meterRequest.request(),
        gatewayRequest.request(),
      ]);
    });

    it('does not dispatch if the selection cannot be found', () => {
      const saved: SelectionState[] = [
        {
          ...initialState,
          id: 1,
          name: 'test 1',
        },
        {
          ...initialState,
          id: 21,
          name: 'test 21',
        },
      ];

      store = configureMockStore({searchParameters: {selection: initialState, saved}});

      store.dispatch(selectSavedSelection({
        ...initialState,
        id: 99,
        name: 'test 99',
      }.id));

      expect(store.getActions()).toEqual([]);
    });

  });

  const onFakeFetchMetersAndGateways = (searchParameters: SearchParameterState) => {
    const encodedUriParameters = getEncodedUriParametersForMeters(searchParameters);
    mockRestClient.onGet(makeUrl('/meters', encodedUriParameters)).reply(200, testData.meters);
    mockRestClient.onGet(makeUrl('/gateways', encodedUriParameters)).reply(200, testData.gateways);
  };

});

import axios from 'axios';
import {routerActions} from 'react-router-redux';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {testData} from '../../../../__tests__/testDataFactory';
import {Period} from '../../../../components/dates/dateModels';
import {makeUrl} from '../../../../helpers/urlFactory';
import {makeRestClient} from '../../../../services/restClient';
import {IdNamed} from '../../../../types/Types';
import {EndPoints, Normalized} from '../../../domain-models/domainModels';
import {requestHandle, RestRequestTypes} from '../../../domain-models/domainModelsActions';
import {Gateway} from '../../../domain-models/gateway/gatewayModels';
import {Meter} from '../../../domain-models/meter/meterModels';
import {SearchParameterState} from '../../searchParameterReducer';
import {
  addSelectionAction, closeSelectionPage, closeSelectionPageAction, deselectSelection, selectPeriod,
  selectPeriodAction, selectSavedSelection, selectSavedSelectionAction, setSelection, setSelectionAction,
  toggleSelection,
} from '../selectionActions';
import {ParameterName, SelectionParameter, SelectionState} from '../selectionModels';
import {initialState, selection} from '../selectionReducer';
import {getEncodedUriParametersForMeters} from '../selectionSelectors';
import MockAdapter = require('axios-mock-adapter');

const configureMockStore = configureStore([thunk]);

describe('selectionActions', () => {

  const meterRequest = requestHandle<Normalized<Meter>>(EndPoints.meters, RestRequestTypes.GET);
  const gatewayRequest = requestHandle<Normalized<Gateway>>(EndPoints.gateways, RestRequestTypes.GET);

  const gothenburg: IdNamed = {...testData.selections.cities[0]};
  const stockholm: IdNamed = {...testData.selections.cities[1]};

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

  describe('toggle selection', () => {

    it('set selection', async () => {
      const rootState = {searchParameters: {selection: {...initialState}, saved: []}};
      onFakeFetchMetersAndGateways(rootState.searchParameters);
      store = configureMockStore(rootState);

      const selection: IdNamed = {...gothenburg};

      const parameter: SelectionParameter = {...selection, parameter: ParameterName.cities};

      store.dispatch(toggleSelection(parameter));

      expect(store.getActions()).toEqual([
        addSelectionAction(parameter),
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
      const state: SelectionState = selection(initialState, addSelectionAction(payload));

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
        addSelectionAction(p1),
        meterRequest.request(),
        gatewayRequest.request(),
        addSelectionAction(p2),
        meterRequest.request(),
        gatewayRequest.request(),
      ]);
    });
  });

  describe('set selection action', () => {
    it('set the selection of one parameter id', () => {

      const rootState = {searchParameters: {selection: {...initialState}, saved: []}};
      onFakeFetchMetersAndGateways(rootState.searchParameters);
      store = configureMockStore(rootState);

      const selection: SelectionParameter = {parameter: ParameterName.cities, ...gothenburg};

      store.dispatch(setSelection(selection));

      expect(store.getActions()).toEqual([
        setSelectionAction(selection),
        meterRequest.request(),
        gatewayRequest.request(),
      ]);
    });
  });

  const onFakeFetchMetersAndGateways = (searchParameters: SearchParameterState) => {
    const encodedUriParameters = getEncodedUriParametersForMeters(searchParameters);
    mockRestClient.onGet(makeUrl('/meters', encodedUriParameters)).reply(200, testData.meters);
    mockRestClient.onGet(makeUrl('/gateways', encodedUriParameters)).reply(200, testData.gateways);
  };

});

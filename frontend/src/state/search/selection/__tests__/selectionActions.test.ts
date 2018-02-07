import axios from 'axios';
import {routerActions} from 'react-router-redux';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {testData} from '../../../../__tests__/testDataFactory';
import {Period} from '../../../../components/dates/dateModels';
import {makeUrl} from '../../../../helpers/urlFactory';
import {makeRestClient} from '../../../../services/restClient';
import {IdNamed} from '../../../../types/Types';
import {Meter} from '../../../domain-models-paginated/meter/meterModels';
import {EndPoints, HttpMethod, Normalized} from '../../../domain-models/domainModels';
import {requestMethod} from '../../../domain-models/domainModelsActions';
import {Gateway} from '../../../domain-models/gateway/gatewayModels';
import {
  addSelectionAction,
  closeSelectionPage,
  closeSelectionPageAction,
  deselectSelection,
  selectPeriod,
  selectPeriodAction,
  selectSavedSelection,
  selectSavedSelectionAction,
  setSelection,
  setSelectionAction,
  toggleSelection,
} from '../selectionActions';
import {ParameterName, SelectionParameter, SelectionState} from '../selectionModels';
import {initialState} from '../selectionReducer';
import {getEncodedUriParametersForMeters, UriLookupState} from '../selectionSelectors';
import MockAdapter = require('axios-mock-adapter');

const configureMockStore = configureStore([thunk]);

describe('selectionActions', () => {

  const meterRequest = requestMethod<Normalized<Meter>>(EndPoints.meters, HttpMethod.GET);
  const gatewayRequest = requestMethod<Normalized<Gateway>>(EndPoints.gateways, HttpMethod.GET);

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
  const rootState = {searchParameters: {selection: {...initialState}, saved}};
  const rootStateNoSaved = {...rootState, searchParameters: {...rootState.searchParameters, saved: []}};

  const onFakeFetchMetersAndGateways = (uriLookupState: UriLookupState) => {
    const encodedUriParameters = getEncodedUriParametersForMeters(uriLookupState);
    mockRestClient.onGet(makeUrl(EndPoints.meters, encodedUriParameters)).reply(200, testData.meters);
    mockRestClient.onGet(makeUrl(EndPoints.gateways, encodedUriParameters)).reply(200, testData.gateways);
  };

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
      store = configureMockStore(rootState);

      onFakeFetchMetersAndGateways(rootState.searchParameters);
      store.dispatch(selectSavedSelection(savedSelection21.id));

      expect(store.getActions()).toEqual([
        selectSavedSelectionAction(savedSelection21),
        meterRequest.request(),
        gatewayRequest.request(),
      ]);
    });

    it('does not dispatch if the selection cannot be found', () => {

      store = configureMockStore(rootState);

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
      const selection: IdNamed = {...gothenburg};
      const parameter: SelectionParameter = {...selection, parameter: ParameterName.cities};
      store = configureMockStore(rootStateNoSaved);

      onFakeFetchMetersAndGateways(rootStateNoSaved.searchParameters);
      store.dispatch(toggleSelection(parameter));

      expect(store.getActions()).toEqual([
        addSelectionAction(parameter),
        meterRequest.request(),
        gatewayRequest.request(),
      ]);
    });

    it('select period', async () => {
      const period = Period.previousMonth;
      store = configureMockStore(rootStateNoSaved);

      onFakeFetchMetersAndGateways(rootStateNoSaved.searchParameters);
      store.dispatch(selectPeriod(period));

      expect(store.getActions()).toEqual([
        selectPeriodAction(period),
        meterRequest.request(),
        gatewayRequest.request(),
      ]);
    });

    it('deselects selected city', () => {
      const selection = {selected: {...initialState, [ParameterName.cities]: [stockholm.id]}};
      const stateWithSelection = {searchParameters: {selection, saved: []}};
      const payload: SelectionParameter = {...stockholm, parameter: ParameterName.cities};
      store = configureMockStore(stateWithSelection);

      onFakeFetchMetersAndGateways(stateWithSelection.searchParameters);
      store.dispatch(toggleSelection(payload));

      expect(store.getActions()).toEqual([
        deselectSelection(payload),
        meterRequest.request(),
        gatewayRequest.request(),
      ]);
    });

    it('set several selections', () => {
      const p1: SelectionParameter = {...stockholm, parameter: ParameterName.cities};
      const p2: SelectionParameter = {...gothenburg, parameter: ParameterName.cities};
      store = configureMockStore(rootStateNoSaved);

      onFakeFetchMetersAndGateways(rootStateNoSaved.searchParameters);
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
      const payload: SelectionParameter = {parameter: ParameterName.cities, ...gothenburg};
      store = configureMockStore(rootState);

      onFakeFetchMetersAndGateways(rootState.searchParameters);
      store.dispatch(setSelection(payload));

      expect(store.getActions()).toEqual([
        setSelectionAction(payload),
        meterRequest.request(),
        gatewayRequest.request(),
      ]);
    });
  });
});

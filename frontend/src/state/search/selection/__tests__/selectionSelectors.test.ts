import {normalize} from 'normalizr';
import {testData} from '../../../../__tests__/testDataFactory';
import {Period} from '../../../../components/dates/dateModels';
import {EndPoints} from '../../../../services/endPoints';
import {IdNamed} from '../../../../types/Types';
import {DomainModelsState, Normalized, SelectionEntity} from '../../../domain-models/domainModels';
import {getRequestOf} from '../../../domain-models/domainModelsActions';
import {
  addresses,
  alarms,
  cities,
  gateways,
  gatewayStatuses,
  initialDomain,
  meterStatuses,
  users,
} from '../../../domain-models/domainModelsReducer';
import {Gateway} from '../../../domain-models/gateway/gatewayModels';
import {selectionsSchema} from '../../../domain-models/selections/selectionsSchemas';
import {User} from '../../../domain-models/user/userModels';
import {initialPaginationState, limit} from '../../../ui/pagination/paginationReducer';
import {ADD_SELECTION, SELECT_PERIOD} from '../selectionActions';
import {
  LookupState,
  ParameterName,
  SelectionListItem,
  SelectionParameter,
  SelectionState,
} from '../selectionModels';
import {initialState, selection} from '../selectionReducer';
import {
  getCities,
  getEncodedUriParametersForMeters,
  getSelectedPeriod,
  getSelection,
  UriLookupStatePaginated,
} from '../selectionSelectors';

describe('selectionSelectors', () => {

  const normalizedSelections = normalize(testData.selections, selectionsSchema);
  const {cities: cityEntities} = normalizedSelections.entities;
  const stockholm: IdNamed = cityEntities['sweden,stockholm'];
  const gothenburg: IdNamed = cityEntities['sweden,göteborg'];
  const vasa: IdNamed = cityEntities['finland,vasa'];

  const selectionsRequest = getRequestOf<Normalized<IdNamed>>(EndPoints.selections);
  const initialSearchParameterState = {selection: {...initialState}, saved: []};
  const initialUriLookupState: UriLookupStatePaginated = {
    ...initialSearchParameterState,
    entityType: 'meters',
    componentId: 'test',
    pagination: initialPaginationState,
  };
  const initialEncodedParameters = getEncodedUriParametersForMeters(initialUriLookupState);
  const initialDomainModelState = initialDomain<SelectionEntity>();
  const domainModels = (domainModelPayload): Partial<DomainModelsState> => ({
    cities: cities(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
    addresses: addresses(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
    alarms: alarms(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
    gatewayStatuses: gatewayStatuses(
      initialDomainModelState,
      selectionsRequest.success(domainModelPayload),
    ),
    gateways: gateways(initialDomain<Gateway>(), {type: 'none'}),
    meterStatuses: meterStatuses(
      initialDomainModelState,
      selectionsRequest.success(domainModelPayload),
    ),
    users: users(initialDomain<User>(), {type: 'none'}),
  });

  it('has entities', () => {
    expect(getSelection({...initialSearchParameterState})).toEqual(initialState);
  });

  it('encode the initial, empty, selection', () => {
    expect(initialEncodedParameters).toEqual(`size=${limit}&page=0`);
  });

  it('gets entities for type city', () => {

    const payload: SelectionParameter = {...stockholm, parameter: ParameterName.cities};

    const state: LookupState = {
      selection: selection(initialState, {type: ADD_SELECTION, payload}),
      domainModels: domainModels(normalizedSelections) as DomainModelsState,
    };

    const stockholmSelected: SelectionListItem[] = [
      {selected: true, ...stockholm},
      {selected: false, ...gothenburg},
      {selected: false, ...vasa},
    ];

    expect(getCities(state)).toEqual(stockholmSelected);
  });

  it('get entities for undefined entity type', () => {
    const domainModelPayload = normalize(testData.selections, selectionsSchema);

    const payload: SelectionParameter = {...stockholm, parameter: ParameterName.cities};
    const notCity: Partial<DomainModelsState> = domainModels(domainModelPayload);
    notCity.cities = cities(initialDomainModelState, {type: 'unknown'});

    const state: LookupState = {
      selection: selection(initialState, {type: ADD_SELECTION, payload}),
      domainModels: notCity as DomainModelsState,
    };

    expect(getCities(state)).toEqual([]);
  });

  describe('encodedUriParameters', () => {

    it('has selected city search parameter', () => {
      const payload: SelectionParameter = {...stockholm, parameter: ParameterName.cities};
      const state: SelectionState = selection(initialState, {type: ADD_SELECTION, payload});

      const encodedUriParametersForMeters = getEncodedUriParametersForMeters({
        selection: state,
        saved: [],
        entityType: 'meters',
        componentId: 'test',
        pagination: initialPaginationState,
      });

      expect(encodedUriParametersForMeters).toEqual(`size=${limit}&page=0&city=sweden%2Cstockholm`);
    });

    it('has two selected cities', () => {
      const payloadGot: SelectionParameter = {...gothenburg, parameter: ParameterName.cities};
      const payloadSto: SelectionParameter = {...stockholm, parameter: ParameterName.cities};
      const prevState: SelectionState = selection(
        initialState,
        {type: ADD_SELECTION, payload: payloadGot},
      );
      const state: SelectionState = selection(
        prevState,
        {type: ADD_SELECTION, payload: payloadSto},
      );

      expect(getEncodedUriParametersForMeters({
        selection: state,
        saved: [],
        entityType: 'meters',
        componentId: 'test',
        pagination: initialPaginationState,
      }))
        .toEqual(`size=${limit}&page=0&city=sweden%2Cg%C3%B6teborg&city=sweden%2Cstockholm`);
    });
  });

  describe('get selected period', () => {

    it('there is a default period', () => {
      expect(getSelectedPeriod(initialState)).toEqual(expect.anything());
    });

    it('get selected period', () => {
      const state: SelectionState = selection(
        initialState,
        {type: SELECT_PERIOD, payload: Period.currentWeek},
      );

      expect(getSelectedPeriod(state)).toBe(Period.currentWeek);
    });
  });

  describe('get subset of cities', () => {

    it('can detect which the selected entities are', () => {
      const payload: SelectionParameter = {...stockholm, parameter: ParameterName.cities};

      const state: LookupState = {
        selection: selection(initialState, {type: ADD_SELECTION, payload}),
        domainModels: domainModels(normalizedSelections) as DomainModelsState,
      };

      const stockholmSelected: SelectionListItem[] = [
        {selected: true, ...stockholm},
        {selected: false, ...gothenburg},
        {selected: false, ...vasa},
      ];

      expect(getCities(state)).toEqual(stockholmSelected);
    });

  });

});

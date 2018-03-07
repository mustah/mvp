import {normalize} from 'normalizr';
import {makeMeter, testData} from '../../../../__tests__/testDataFactory';
import {Period} from '../../../../components/dates/dateModels';
import {IdNamed} from '../../../../types/Types';
import {Meter} from '../../../domain-models-paginated/meter/meterModels';
import {
  DomainModelsState,
  EndPoints,
  Normalized,
  NormalizedState,
  SelectionEntity,
} from '../../../domain-models/domainModels';
import {getRequestOf} from '../../../domain-models/domainModelsActions';
import {
  addresses,
  alarms,
  cities,
  gateways,
  gatewayStatuses,
  initialDomain,
  manufacturers,
  meterStatuses,
  productModels,
  users,
} from '../../../domain-models/domainModelsReducer';
import {selectionsSchema} from '../../../domain-models/domainModelsSchemas';
import {Gateway} from '../../../domain-models/gateway/gatewayModels';
import {User} from '../../../domain-models/user/userModels';
import {initialPaginationState, limit} from '../../../ui/pagination/paginationReducer';
import {ADD_SELECTION, SELECT_PERIOD} from '../selectionActions';
import {
  LookupState,
  ParameterName,
  SelectionListItem,
  SelectionParameter,
  SelectionState,
  SelectionSummary,
} from '../selectionModels';
import {initialState, selection} from '../selectionReducer';
import {
  getCities,
  getEncodedUriParametersForMeters,
  getSelectedPeriod,
  getSelection,
  getSelectionSummary,
  UriLookupStatePaginated,
} from '../selectionSelectors';

describe('selectionSelectors', () => {

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
    addresses: addresses(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
    alarms: alarms(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
    cities: cities(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
    gatewayStatuses: gatewayStatuses(
      initialDomainModelState,
      selectionsRequest.success(domainModelPayload),
    ),
    gateways: gateways(initialDomain<Gateway>(), {type: 'none'}),
    manufacturers: manufacturers(
      initialDomainModelState,
      selectionsRequest.success(domainModelPayload),
    ),
    meterStatuses: meterStatuses(
      initialDomainModelState,
      selectionsRequest.success(domainModelPayload),
    ),
    productModels: productModels(
      initialDomainModelState,
      selectionsRequest.success(domainModelPayload),
    ),
    users: users(initialDomain<User>(), {type: 'none'}),
  });

  const gothenburg: IdNamed = {...testData.selections.cities[0]};
  const stockholm: IdNamed = {...testData.selections.cities[1]};

  it('has entities', () => {
    expect(getSelection({...initialSearchParameterState})).toEqual(initialState);
  });

  it('encode the initial, empty, selection', () => {
    expect(initialEncodedParameters).toEqual(`size=${limit}&page=0`);
  });

  it('gets entities for type city', () => {
    const domainModelPayload = normalize(testData.selections, selectionsSchema);

    const payload: SelectionParameter = {...stockholm, parameter: ParameterName.cities};

    const state: LookupState = {
      selection: selection(initialState, {type: ADD_SELECTION, payload}),
      domainModels: domainModels(domainModelPayload) as DomainModelsState,
    };

    const stockholmSelected: SelectionListItem[] = [
      {selected: true, id: 'sto', name: 'Stockholm'},
      {selected: false, id: 'got', name: 'Göteborg'},
      {selected: false, id: 'kub', name: 'Kungsbacka'},
      {selected: false, id: 'mmx', name: 'Malmö'},
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

      expect(encodedUriParametersForMeters).toEqual(`size=${limit}&page=0&city.id=sto`);
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
        .toEqual(`size=${limit}&page=0&city.id=got&city.id=sto`);
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

      const domainModelPayload = normalize(testData.selections, selectionsSchema);

      const state: LookupState = {
        selection: selection(initialState, {type: ADD_SELECTION, payload}),
        domainModels: domainModels(domainModelPayload) as DomainModelsState,
      };

      const stockholmSelected: SelectionListItem[] = [
        {selected: true, id: 'sto', name: 'Stockholm'},
        {selected: false, id: 'got', name: 'Göteborg'},
        {selected: false, id: 'kub', name: 'Kungsbacka'},
        {selected: false, id: 'mmx', name: 'Malmö'},
      ];

      expect(getCities(state)).toEqual(stockholmSelected);
    });

  });

  describe('summary of meters in selection', () => {

    const emptySelection = (): SelectionSummary => ({addresses: 0, cities: 0, meters: 0});

    it('handles an empty selection', () => {
      const meterState: NormalizedState<Meter> = {
        isFetching: false,
        isSuccessfullyFetched: false,
        total: 0,
        result: [],
        entities: {},
      };

      const summary = getSelectionSummary(meterState);
      expect(summary).toEqual(emptySelection());
    });

    it('groups meters into cities and addresses', () => {
      const meterState: NormalizedState<Meter> = {
        isFetching: false,
        isSuccessfullyFetched: true,
        total: 4,
        result: ['1', '2', '3', '4'],
        entities: {
          1: makeMeter(1, 1, 'Helsingborg', 1, 'Storgatan 5'),
          2: makeMeter(2, 1, 'Helsingborg', 2, 'Storgatan 6'),
          3: makeMeter(3, 2, 'Luleå', 3, 'Ringvägen 7'),
          4: makeMeter(4, 2, 'Luleå', 3, 'Ringvägen 7'),
        },
      };

      const selection = emptySelection();
      selection.meters = 4;
      selection.cities = 2;
      selection.addresses = 3;

      const summary = getSelectionSummary(meterState);
      expect(summary).toEqual(selection);
    });

  });

});

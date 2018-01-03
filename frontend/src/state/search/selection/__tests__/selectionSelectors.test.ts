import {normalize} from 'normalizr';
import {makeMeter, testData} from '../../../../__tests__/testDataFactory';
import {Period} from '../../../../components/dates/dateModels';
import {IdNamed} from '../../../../types/Types';
import {DomainModelsState, NormalizedState, SelectionEntity} from '../../../domain-models/domainModels';
import {selectionsRequest} from '../../../domain-models/domainModelsActions';
import {
  addresses,
  alarms,
  cities,
  gateways,
  gatewayStatuses,
  initialDomain,
  manufacturers,
  meters,
  meterStatuses,
  productModels, users,
} from '../../../domain-models/domainModelsReducer';
import {selectionsSchema} from '../../../domain-models/domainModelsSchemas';
import {Gateway} from '../../../domain-models/gateway/gatewayModels';
import {Meter} from '../../../domain-models/meter/meterModels';
import {SearchParameterState} from '../../searchParameterReducer';
import {addSelectionAction, selectPeriodAction} from '../selectionActions';
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
} from '../selectionSelectors';
import {User} from '../../../domain-models/user/userModels';

describe('selectionSelectors', () => {

  const initialSearchParametersState: SearchParameterState = {selection: {...initialState}, saved: []};
  const initialEncodedParameters = getEncodedUriParametersForMeters(initialSearchParametersState);

  const initialDomainModelState = initialDomain<SelectionEntity>();

  const gothenburg: IdNamed = {...testData.selections.cities[0]};
  const stockholm: IdNamed = {...testData.selections.cities[1]};

  it('has entities', () => {
    expect(getSelection({...initialSearchParametersState})).toEqual(initialState);
  });

  it('encode the initial, empty, selection', () => {
    expect(initialEncodedParameters).toEqual('');
  });

  it('gets entities for type city', () => {
    const domainModelPayload = normalize(testData.selections, selectionsSchema);
    const domainModels: DomainModelsState = {
      meters: meters(initialDomain<Meter>(), {type: 'none'}),
      gateways: gateways(initialDomain<Gateway>(), {type: 'none'}),
      users: users(initialDomain<User>(), {type: 'none'}),
      alarms: alarms(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
      manufacturers: manufacturers(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
      productModels: productModels(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
      meterStatuses: meterStatuses(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
      gatewayStatuses: gatewayStatuses(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
      addresses: addresses(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
      cities: cities(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
    };

    const payload: SelectionParameter = {...stockholm, parameter: ParameterName.cities};

    const state: LookupState = {
      selection: selection(initialState, addSelectionAction(payload)),
      domainModels,
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
    const domainModels: DomainModelsState = {
      meters: meters(initialDomain<Meter>(), {type: 'none'}),
      gateways: gateways(initialDomain<Gateway>(), {type: 'none'}),
      users: users(initialDomain<User>(), {type: 'none'}),
      alarms: alarms(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
      manufacturers: manufacturers(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
      productModels: productModels(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
      meterStatuses: meterStatuses(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
      gatewayStatuses: gatewayStatuses(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
      addresses: addresses(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
      cities: cities(initialDomainModelState, {type: 'unknown'}),
    };

    const payload: SelectionParameter = {...stockholm, parameter: ParameterName.cities};

    const state: LookupState = {
      selection: selection(initialState, addSelectionAction(payload)),
      domainModels,
    };

    expect(getCities(state)).toEqual([]);
  });

  describe('encodedUriParameters', () => {

    it('has selected city search parameter', () => {
      const payload: SelectionParameter = {...stockholm, parameter: ParameterName.cities};
      const state: SelectionState = selection(initialState, addSelectionAction(payload));

      const encodedUriParametersForMeters = getEncodedUriParametersForMeters({selection: state, saved: []});

      expect(encodedUriParametersForMeters).toEqual('city.id=sto');
    });

    it('has two selected cities', () => {
      const payloadGot: SelectionParameter = {...gothenburg, parameter: ParameterName.cities};
      const payloadSto: SelectionParameter = {...stockholm, parameter: ParameterName.cities};
      const prevState: SelectionState = selection(initialState, addSelectionAction(payloadGot));
      const state: SelectionState = selection(prevState, addSelectionAction(payloadSto));

      expect(getEncodedUriParametersForMeters({selection: state, saved: []}))
        .toEqual('city.id=got&city.id=sto');
    });
  });

  describe('get selected period', () => {

    it('there is a default period', () => {
      expect(getSelectedPeriod(initialState)).toEqual(expect.anything());
    });

    it('get selected period', () => {
      const state: SelectionState = selection(initialState, selectPeriodAction(Period.currentWeek));

      expect(getSelectedPeriod(state)).toBe(Period.currentWeek);
    });
  });

  describe('get subset of cities', () => {

    it('can detect which the selected entities are', () => {
      const payload: SelectionParameter = {...stockholm, parameter: ParameterName.cities};

      const domainModelPayload = normalize(testData.selections, selectionsSchema);
      const domainModels: DomainModelsState = {
        meters: meters(initialDomain<Meter>(), {type: 'none'}),
        gateways: gateways(initialDomain<Gateway>(), {type: 'none'}),
        users: users(initialDomain<User>(), {type: 'none'}),
        alarms: alarms(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
        manufacturers: manufacturers(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
        productModels: productModels(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
        meterStatuses: meterStatuses(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
        gatewayStatuses: gatewayStatuses(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
        addresses: addresses(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
        cities: cities(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
      };

      const state: LookupState = {
        selection: selection(initialState, addSelectionAction(payload)),
        domainModels,
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

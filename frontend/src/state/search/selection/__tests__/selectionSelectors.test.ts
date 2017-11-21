import {normalize} from 'normalizr';
import {testData} from '../../../../__tests__/TestDataFactory';
import {IdNamed, Period} from '../../../../types/Types';
import {DomainModelsState, SelectionEntity} from '../../../domain-models/domainModels';
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
  productModels,
} from '../../../domain-models/domainModelsReducer';
import {selectionsSchema} from '../../../domain-models/domainModelsSchemas';
import {Gateway} from '../../../domain-models/gateway/gatewayModels';
import {Meter} from '../../../domain-models/meter/meterModels';
import {SearchParameterState} from '../../searchParameterReducer';
import {selectPeriodAction, setSelection} from '../selectionActions';
import {LookupState, parameterNames, SelectionListItem, SelectionParameter, SelectionState} from '../selectionModels';
import {initialState, selection} from '../selectionReducer';
import {getCities, getEncodedUriParametersForMeters, getSelectedPeriod, getSelection} from '../selectionSelectors';

describe('selectionSelectors', () => {

  const initialSearchParametersState: SearchParameterState = {selection: {...initialState}, saved: []};
  const initialEncodedParameters = getEncodedUriParametersForMeters(initialSearchParametersState);

  const initialDomainModelState = initialDomain<SelectionEntity>();

  const gothenburg: IdNamed = {...testData.selections.cities[0]};
  const stockholm: IdNamed = {...testData.selections.cities[1]};

  it('has entities', () => {
    expect(getSelection({...initialSearchParametersState})).toEqual(initialState);
  });

  it('gets entities for type city', () => {
    const domainModelPayload = normalize(testData.selections, selectionsSchema);
    const domainModels: DomainModelsState = {
      meters: meters(initialDomain<Meter>(), {type: 'none'}),
      gateways: gateways(initialDomain<Gateway>(), {type: 'none'}),
      alarms: alarms(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
      manufacturers: manufacturers(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
      productModels: productModels(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
      meterStatuses: meterStatuses(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
      gatewayStatuses: gatewayStatuses(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
      addresses: addresses(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
      cities: cities(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
    };

    const payload: SelectionParameter = {...stockholm, parameter: parameterNames.cities};

    const state: LookupState = {
      selection: selection(initialState, setSelection(payload)),
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
      alarms: alarms(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
      manufacturers: manufacturers(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
      productModels: productModels(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
      meterStatuses: meterStatuses(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
      gatewayStatuses: gatewayStatuses(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
      addresses: addresses(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
      cities: cities(initialDomainModelState, {type: 'unknown'}),
    };

    const payload: SelectionParameter = {...stockholm, parameter: parameterNames.cities};

    const state: LookupState = {
      selection: selection(initialState, setSelection(payload)),
      domainModels,
    };

    expect(getCities(state)).toEqual([]);
  });

  describe('encodedUriParameters', () => {

    it('has selected city search parameter', () => {
      const payload: SelectionParameter = {...stockholm, parameter: parameterNames.cities};
      const state: SelectionState = selection(initialState, setSelection(payload));

      const encodedUriParametersForMeters = getEncodedUriParametersForMeters({selection: state, saved: []});

      expect(encodedUriParametersForMeters).toEqual('city.id=sto&' + initialEncodedParameters);
    });

    it('has two selected cities', () => {
      const payloadGot: SelectionParameter = {...gothenburg, parameter: parameterNames.cities};
      const payloadSto: SelectionParameter = {...stockholm, parameter: parameterNames.cities};
      const prevState: SelectionState = selection(initialState, setSelection(payloadGot));
      const state: SelectionState = selection(prevState, setSelection(payloadSto));

      expect(getEncodedUriParametersForMeters({selection: state, saved: []}))
        .toEqual('city.id=got&city.id=sto&' + initialEncodedParameters);
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

  describe('get sub set of cities', () => {

    it('can detect which the selected entities are', () => {
      const payload: SelectionParameter = {...stockholm, parameter: parameterNames.cities};

      const domainModelPayload = normalize(testData.selections, selectionsSchema);
      const domainModels: DomainModelsState = {
        meters: meters(initialDomain<Meter>(), {type: 'none'}),
        gateways: gateways(initialDomain<Gateway>(), {type: 'none'}),
        alarms: alarms(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
        manufacturers: manufacturers(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
        productModels: productModels(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
        meterStatuses: meterStatuses(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
        gatewayStatuses: gatewayStatuses(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
        addresses: addresses(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
        cities: cities(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
      };

      const state: LookupState = {
        selection: selection(initialState, setSelection(payload)),
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

});

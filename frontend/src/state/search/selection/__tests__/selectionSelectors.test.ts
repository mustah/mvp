import {normalize} from 'normalizr';
import {testData} from '../../../../__tests__/TestDataFactory';
import {IdNamed, Period} from '../../../../types/Types';
import {DomainModel, SelectionEntityState} from '../../../domain-models/domainModels';
import {addresses, cities, initialState as initialDomainModelState} from '../../../domain-models/domainModelsReducer';
import {selectionsSchema} from '../../../domain-models/domainModelsSchemas';
import {SearchParameterState} from '../../searchParameterReducer';
import {selectPeriodAction, setSelection} from '../selectionActions';
import {LookupState, parameterNames, SelectionListItem, SelectionParameter, SelectionState} from '../selectionModels';
import {initialState, selection} from '../selectionReducer';
import {getCities, getEncodedUriParameters, getSelectedPeriod, getSelection} from '../selectionSelectors';
import {selectionsRequest} from '../../../domain-models/domainModelsActions';

describe('selectionSelectors', () => {

  const initialSearchParametersState: SearchParameterState = {selection: {...initialState}, saved: []};
  const initialEncodedParameters = getEncodedUriParameters(initialSearchParametersState);

  const gothenburg: IdNamed = {...testData.selections.cities[0]};
  const stockholm: IdNamed = {...testData.selections.cities[1]};

  it('has entities', () => {
    expect(getSelection({...initialSearchParametersState})).toEqual(initialState);
  });

  it('gets entities for type city', () => {
    const domainModelPayload = normalize(testData.selections, selectionsSchema);
    const selectionEntities: DomainModel<SelectionEntityState> = {
      addresses: addresses(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
      cities: cities(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
    };

    const payload: SelectionParameter = {...stockholm, parameter: parameterNames.cities};

    const state: LookupState = {
      selection: selection(initialState, setSelection(payload)),
      selectionEntities,
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
    const selectionEntities: DomainModel<SelectionEntityState> = {
      addresses: addresses(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
      cities: cities(initialDomainModelState, {type: 'unknown'}),
    };

    const payload: SelectionParameter = {...stockholm, parameter: parameterNames.cities};

    const state: LookupState = {
      selection: selection(initialState, setSelection(payload)),
      selectionEntities,
    };

    expect(getCities(state)).toEqual([]);
  });

  describe('encodedUriParameters', () => {

    it('has selected city search parameter', () => {
      const payload: SelectionParameter = {...stockholm, parameter: parameterNames.cities};
      const state: SelectionState = selection(initialState, setSelection(payload));

      expect(getEncodedUriParameters({selection: state, saved: []})).toEqual('city.id=sto&' + initialEncodedParameters);
    });

    it('has two selected cities', () => {
      const payloadGot: SelectionParameter = {...gothenburg, parameter: parameterNames.cities};
      const payloadSto: SelectionParameter = {...stockholm, parameter: parameterNames.cities};
      const prevState: SelectionState = selection(initialState, setSelection(payloadGot));
      const state: SelectionState = selection(prevState, setSelection(payloadSto));

      expect(getEncodedUriParameters({selection: state, saved: []}))
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
      const selectionEntities: DomainModel<SelectionEntityState> = {
        addresses: addresses(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
        cities: cities(initialDomainModelState, selectionsRequest.success(domainModelPayload)),
      };

      const state: LookupState = {
        selection: selection(initialState, setSelection(payload)),
        selectionEntities,
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

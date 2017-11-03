import {normalize} from 'normalizr';
import {testData} from '../../../../__tests__/TestDataFactory';
import {IdNamed, Period} from '../../../../types/Types';
import {geoDataSuccess} from '../../../domain-models/geoData/geoDataActions';
import {GeoDataState} from '../../../domain-models/geoData/geoDataModels';
import {addresses, cities, initialState as initialGeoDataState} from '../../../domain-models/geoData/geoDataReducer';
import {geoDataSchema} from '../../../domain-models/geoData/geoDataSchemas';
import {SearchParameterState} from '../../searchParameterReducer';
import {selectPeriodAction, setSelection} from '../selectionActions';
import {LookupState, parameterNames, SelectionParameter, SelectionState} from '../selectionModels';
import {initialState, selection} from '../selectionReducer';
import {
  getDeselectedCities,
  getEncodedUriParameters,
  getSelectedCities,
  getSelectedPeriod,
  getSelection,
} from '../selectionSelectors';

describe('selectionSelectors', () => {

  const searchParametersState: SearchParameterState = {selection: {...initialState}};

  const gothenburg: IdNamed = {...testData.geoData.cities[0]};
  const stockholm: IdNamed = {...testData.geoData.cities[1]};

  it('has entities', () => {
    expect(getSelection({...searchParametersState})).toEqual(initialState);
  });

  it('gets entities for type city', () => {
    const payload: SelectionParameter = {...stockholm, parameter: parameterNames.cities};

    const geoDataPayload = normalize(testData.geoData, geoDataSchema);
    const geoDataState: GeoDataState = {
      addresses: addresses(initialGeoDataState, geoDataSuccess(geoDataPayload)),
      cities: cities(initialGeoDataState, geoDataSuccess(geoDataPayload)),
    };

    const state: LookupState = {
      selection: selection(initialState, setSelection(payload)),
      geoData: geoDataState,
    };

    expect(getSelectedCities(state)).toEqual([{...stockholm}]);
  });

  describe('encodedUriParameters', () => {

    it('has no search parameters', () => {
      expect(getEncodedUriParameters(searchParametersState)).toEqual('period=now');
    });

    it('has selected city search parameter', () => {
      const payload: SelectionParameter = {...stockholm, parameter: parameterNames.cities};
      const state: SelectionState = selection(initialState, setSelection(payload));

      expect(getEncodedUriParameters({selection: state})).toEqual('city=sto&period=now');
    });

    it('has two selected cities', () => {
      const payloadGot: SelectionParameter = {...gothenburg, parameter: parameterNames.cities};
      const payloadSto: SelectionParameter = {...stockholm, parameter: parameterNames.cities};
      const prevState: SelectionState = selection(initialState, setSelection(payloadGot));
      const state: SelectionState = selection(prevState, setSelection(payloadSto));

      expect(getEncodedUriParameters({selection: state})).toEqual('city=got&city=sto&period=now');
    });
  });

  describe('get selected period', () => {

    it('period now is default ', () => {
      expect(getSelectedPeriod(initialState)).toBe(Period.now);
    });

    it('get selected period', () => {
      const state: SelectionState = selection(initialState, selectPeriodAction(Period.month));

      expect(getSelectedPeriod(state)).toBe(Period.month);
    });
  });

  describe('get deselected cities', () => {

    it('period now is default ', () => {
      const payload: SelectionParameter = {...stockholm, parameter: parameterNames.cities};

      const geoDataPayload = normalize(testData.geoData, geoDataSchema);
      const geoDataState: GeoDataState = {
        addresses: addresses(initialGeoDataState, geoDataSuccess(geoDataPayload)),
        cities: cities(initialGeoDataState, geoDataSuccess(geoDataPayload)),
      };

      const state: LookupState = {
        selection: selection(initialState, setSelection(payload)),
        geoData: geoDataState,
      };

      expect(getDeselectedCities(state)).toEqual([
        {id: 'got', name: 'Göteborg'},
        {id: 'mmx', name: 'Malmö'},
        {id: 'kub', name: 'Kungsbacka'},
      ]);
    });

  });

});

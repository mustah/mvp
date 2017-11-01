import {IdNamed} from '../../../../types/Types';
import {SearchParameterState} from '../../searchParameterReducer';
import {setSelection} from '../selectionActions';
import {parameterNames, SelectionParameter} from '../selectionModels';
import {addCityEntity, initialState, selection, SelectionState} from '../selectionReducer';
import {getEncodedUriParameters, getSelectedCities, getSelection, isFetching} from '../selectionSelectors';

const dbJsonData = require('./../../../../../mockdata');
const mockData = dbJsonData();

describe('selectionSelectors', () => {

  const searchParametersState: SearchParameterState = {selection: {...initialState}};

  const gothenburg: IdNamed = {...mockData.selections.cities[0]};
  const stockholm: IdNamed = {...mockData.selections.cities[1]};

  it('has entities', () => {
    expect(getSelection({...searchParametersState})).toEqual(initialState);
  });

  it('is not fetching initially ', () => {
    expect(isFetching({...searchParametersState})).toBe(false);
  });

  it('is fetching', () => {
    const state = {selection: {...initialState, isFetching: true}};

    expect(isFetching(state)).toBe(true);
  });

  it('gets entities for type city', () => {
    const prevState: SelectionState = addCityEntity(initialState, {...stockholm});
    const payload: SelectionParameter = {...stockholm, parameter: parameterNames.cities};
    const state: SelectionState = selection(prevState, setSelection(payload));

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

});

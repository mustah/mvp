import {IdNamed} from '../../../../types/Types';
import {SearchParameterState} from '../../searchParameterReducer';
import {setSelection} from '../selectionActions';
import {SelectionAttribute} from '../selectionModels';
import {addCityEntity, initialState, selection, SelectionState} from '../selectionReducer';
import {getEncodedUriParameters, getSelectedCities, getSelection, isFetching} from '../selectionSelectors';

describe('selectionSelectors', () => {

  const searchParametersState: SearchParameterState = {selection: {...initialState}};

  const stockholm: IdNamed = {
    id: 'sto',
    name: 'Stockholm',
  };

  const gothenburg: IdNamed = {
    id: 'got',
    name: 'Göteborg',
  };

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
    const payload = {...stockholm, attribute: SelectionAttribute.cities};
    const state: SelectionState = selection(prevState, setSelection(payload));

    expect(getSelectedCities(state)).toEqual([{...stockholm}]);
  });

  describe('encodedUriParameters', () => {

    it('has no search parameters', () => {
      expect(getEncodedUriParameters(searchParametersState)).toEqual('');
    });

    it('has selected city search parameter', () => {
      const payload = {...stockholm, attribute: SelectionAttribute.cities};
      const state: SelectionState = selection(initialState, setSelection(payload));

      expect(getEncodedUriParameters({selection: state})).toEqual('city=sto');
    });

    it('has two selected cities', () => {
      const payloadGot = {...gothenburg, attribute: SelectionAttribute.cities};
      const payloadSto = {...stockholm, attribute: SelectionAttribute.cities};
      const prevState: SelectionState = selection(initialState, setSelection(payloadGot));
      const state: SelectionState = selection(prevState, setSelection(payloadSto));

      expect(getEncodedUriParameters({selection: state})).toEqual('city=got&city=sto');
    });
  });

});

import {normalize} from 'normalizr';
import {testData} from '../../../../__tests__/TestDataFactory';
import {geoDataFailure, geoDataRequest, geoDataSuccess} from '../geoDataActions';
import {addresses, cities, initialState} from '../geoDataReducer';
import {geoDataSchema} from '../geoDataSchemas';

describe('geoDataReducer', () => {

  describe('addresses', () => {

    it('has initial state', () => {
      expect(addresses(initialState, {type: 'unknown'})).toEqual({...initialState});
    });

    it('fetches geoData', () => {
      expect(addresses(initialState, geoDataRequest())).toEqual({...initialState, isFetching: true});
    });

    it('has fetched geoData successfully ', () => {
      const payload = normalize(testData.geoData, geoDataSchema);

      expect(addresses(initialState, geoDataSuccess(payload))).toEqual({
        ...initialState,
        entities: {
          1: {id: 1, name: 'Stampgatan 46'},
          2: {id: 2, name: 'Stampgatan 33'},
          3: {id: 3, name: 'Kungsgatan 44'},
          4: {id: 4, name: 'Drottninggatan 1'},
          5: {id: 5, name: 'Åvägen 9'},
        },
        result: [1, 2, 3, 4, 5],
        total: 5,
      });
    });

    it('has error when fetching has failed', () => {
      const payload = {message: 'failed'};

      expect(addresses(initialState, geoDataFailure(payload))).toEqual({
        ...initialState,
        error: payload,
      });
    });
  });

  describe('cities', () => {

    it('has initial state', () => {
      expect(cities(initialState, {type: 'unknown'})).toEqual({...initialState});
    });

    it('fetches geoData', () => {
      expect(cities(initialState, geoDataRequest())).toEqual({...initialState, isFetching: true});
    });

    it('has fetched geoData successfully', () => {
      const payload = normalize(testData.geoData, geoDataSchema);

      expect(cities(initialState, geoDataSuccess(payload))).toEqual({
        ...initialState,
        entities: {
          got: {id: 'got', name: 'Göteborg'},
          sto: {id: 'sto', name: 'Stockholm'},
          mmx: {id: 'mmx', name: 'Malmö'},
          kub: {id: 'kub', name: 'Kungsbacka'},
        },
        result: ['got', 'sto', 'mmx', 'kub'],
        total: 4,
      });
    });

    it('has error when fetching has failed', () => {
      const payload = {message: 'failed'};

      expect(cities(initialState, geoDataFailure(payload))).toEqual({
        ...initialState,
        error: payload,
      });
    });
  });

});

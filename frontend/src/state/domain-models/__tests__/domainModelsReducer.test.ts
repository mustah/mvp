import {normalize} from 'normalizr';
import {testData} from '../../../__tests__/TestDataFactory';
import {domainModelFailure, domainModelRequest, domainModelSuccess} from '../domainModelsActions';
import {addresses, cities, initialState} from '../domainModelsReducer';
import {selectionsSchema} from '../domainModelsSchemas';

describe('domainModelsReducer', () => {

  describe('addresses', () => {

    it('has initial state', () => {
      expect(addresses(initialState, {type: 'unknown'})).toEqual({...initialState});
    });

    it('fetches selections for addresses', () => {
      expect(addresses(initialState, domainModelRequest())).toEqual({...initialState, isFetching: true});
    });

    it('has fetched selections successfully ', () => {
      const payload = normalize(testData.selections, selectionsSchema);

      expect(addresses(initialState, domainModelSuccess(payload))).toEqual({
        ...initialState,
        entities: {
          1: {id: 1, name: 'Stampgatan 46', cityId: 'got'},
          2: {id: 2, name: 'Stampgatan 33', cityId: 'got'},
          3: {id: 3, name: 'Kungsgatan 44', cityId: 'sto'},
          4: {id: 4, name: 'Drottninggatan 1', cityId: 'mmx'},
          5: {id: 5, name: 'Åvägen 9', cityId: 'kub'},
        },
        result: [1, 2, 3, 4, 5],
        total: 5,
      });
    });

    it('has error when fetching has failed', () => {
      const payload = {message: 'failed'};

      expect(addresses(initialState, domainModelFailure(payload))).toEqual({
        ...initialState,
        error: payload,
      });
    });
  });

  describe('cities', () => {

    it('has initial state', () => {
      expect(cities(initialState, {type: 'unknown'})).toEqual({...initialState});
    });

    it('fetches selections for cities', () => {
      expect(cities(initialState, domainModelRequest())).toEqual({...initialState, isFetching: true});
    });

    it('has fetched selections successfully', () => {
      const payload = normalize(testData.selections, selectionsSchema);

      expect(cities(initialState, domainModelSuccess(payload))).toEqual({
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

      expect(cities(initialState, domainModelFailure(payload))).toEqual({
        ...initialState,
        error: payload,
      });
    });
  });

});

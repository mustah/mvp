import axios from 'axios';
import {normalize} from 'normalizr';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {makeRestClient} from '../../../../services/restClient';
import {fetchGeoData, geoDataFailure, geoDataRequest, geoDataSuccess} from '../geoDataActions';
import {geoDataSchema} from '../geoDataSchemas';
import MockAdapter = require('axios-mock-adapter');
import {testData} from '../../../../__tests__/TestDataFactory';

const configureMockStore = configureStore([thunk]);

describe('geoDataActions', () => {

  let mockRestClient;
  let store;

  beforeEach(() => {
    store = configureMockStore({});
    mockRestClient = new MockAdapter(axios);
    makeRestClient('test');
  });

  afterEach(() => {
    mockRestClient.reset();
  });

  describe('fetch geoData', () => {

    it('normalizes data', async () => {
      await fetchFakeGeoData();

      expect(store.getActions()).toEqual([
        geoDataRequest(),
        geoDataSuccess(normalize(testData.geoData, geoDataSchema)),
      ]);
    });

    it('throws exception when no data exists', async () => {
      const response = {message: 'failed'};

      mockRestClient.onGet('/selections').reply(401, response);

      await store.dispatch(fetchGeoData());

      expect(store.getActions()).toEqual([
        geoDataRequest(),
        geoDataFailure({...response}),
      ]);
    });
  });

  const fetchFakeGeoData = async () => {
    mockRestClient.onGet('/selections').reply(200, testData.geoData);

    return store.dispatch(fetchGeoData());
  };

});

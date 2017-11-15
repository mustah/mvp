import axios from 'axios';
import {normalize} from 'normalizr';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {testData} from '../../../__tests__/TestDataFactory';
import {makeRestClient} from '../../../services/restClient';
import {fetchDomainModel, domainModelFailure, domainModelRequest, domainModelSuccess} from '../domainModelsActions';
import {selectionsSchema} from '../domainModelsSchemas';
import MockAdapter = require('axios-mock-adapter');

const configureMockStore = configureStore([thunk]);

describe('domainModelsActions', () => {

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

  describe('fetch domainModels from /selections', () => {

    it('normalizes data', async () => {
      await fetchFakeDomainModels();

      expect(store.getActions()).toEqual([
        domainModelRequest(),
        domainModelSuccess(normalize(testData.selections, selectionsSchema)),
      ]);
    });

    it('throws exception when no data exists', async () => {
      const response = {message: 'failed'};

      mockRestClient.onGet('/selections').reply(401, response);

      await store.dispatch(fetchDomainModel());

      expect(store.getActions()).toEqual([
        domainModelRequest(),
        domainModelFailure({...response}),
      ]);
    });
  });

  const fetchFakeDomainModels = async () => {
    mockRestClient.onGet('/selections').reply(200, testData.selections);

    return store.dispatch(fetchDomainModel());
  };

});

import axios from 'axios';
import {normalize} from 'normalizr';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {makeMeter} from '../../../__tests__/testDataFactory';
import {initLanguage} from '../../../i18n/i18n';
import {RootState} from '../../../reducers/rootReducer';
import {makeRestClient} from '../../../services/restClient';
import {ErrorResponse} from '../../../types/Types';
import {Meter} from '../../domain-models-paginated/meter/meterModels';
import {meterSchema} from '../../domain-models-paginated/meter/meterSchema';
import {NormalizedPaginated} from '../../domain-models-paginated/paginatedDomainModels';
import {fetchMeters, requestMethodPaginated} from '../../domain-models-paginated/paginatedDomainModelsActions';
import {showFailMessage} from '../../ui/message/messageActions';
import {paginationUpdateMetaData} from '../../ui/pagination/paginationActions';
import {EndPoints} from '../domainModels';
import MockAdapter = require('axios-mock-adapter');

initLanguage({code: 'en', name: 'english'});
const configureMockStore = configureStore([thunk]);
let store;
let mockRestClient;

describe('paginatedDomainModelsActions', () => {

  const initialRootState: Partial<RootState> = {
    paginatedDomainModels: {
      meters: {entities: {}, result: {}},
    },
  };

  const requestMeters =
    requestMethodPaginated<NormalizedPaginated<Meter>>(EndPoints.meters);

  beforeEach(() => {
    store = configureMockStore(initialRootState);
    mockRestClient = new MockAdapter(axios);
    makeRestClient('test');
  });

  afterEach(() => {
    mockRestClient.reset();
  });

  describe('fetch meters from /meters', () => {
    const page = 0;
    const meter1: Meter = makeMeter(1, 1, 'gothenburg', 1, 'kungsgatan');
    const meter2: Meter = makeMeter(2, 2, 'stockholm', 2, 'kungsgatan');

    const meterResponse = {
      content: [
        meter1,
        meter2,
      ],
      totalPages: 1440,
      totalElements: 28800,
      last: false,
      size: 2,
      number: 0,
      first: true,
      numberOfElements: 2,
      sort: null,
    };

    const getMetersWithResponseOk = async (page: number) => {
      mockRestClient.onGet(EndPoints.meters).reply(200, meterResponse);
      return store.dispatch(fetchMeters(page));
    };

    const getMetersWithBadRequest = async (page: number, error: ErrorResponse | undefined) => {
      mockRestClient.onGet(EndPoints.meters).reply(401, error);
      return store.dispatch(fetchMeters(page));
    };

    const normalizedMeterResponse = (page: number): NormalizedPaginated<Meter> => ({
      page,
      ...normalize(meterResponse, meterSchema),
    });

    it('normalizes data on successful request', async () => {

      await getMetersWithResponseOk(page);

      expect(store.getActions()).toEqual([
        requestMeters.request(page),
        requestMeters.success({...normalizedMeterResponse(page)}),
        paginationUpdateMetaData({model: 'meters', ...normalizedMeterResponse(page).result}),
      ]);
    });

    it('get an error message back on a bad request with no response from backend', async () => {
      await getMetersWithBadRequest(page, undefined);

      expect(store.getActions()).toEqual([
        requestMeters.request(page),
        requestMeters.failure({page, message: 'An unexpected error occurred'}),
        showFailMessage('Error: An unexpected error occurred'),
      ]);
    });

    it('get an error message back on a bad request and response from backend', async () => {
      const errorResponse: ErrorResponse = {message: 'an error'};

      await getMetersWithBadRequest(page, errorResponse);

      expect(store.getActions()).toEqual([
        requestMeters.request(page),
        requestMeters.failure({page, ...errorResponse}),
        showFailMessage('Error: an error'),
      ]);
    });

    it('request a get to an already fetched page that have no result list', async () => {
      const existingPage = 1;
      const initialState: Partial<RootState> = {
        paginatedDomainModels: {
          meters: {entities: {}, result: {[existingPage]: {isFetching: false}}},
        },
      };
      store = configureMockStore(initialState);

      await getMetersWithResponseOk(existingPage);

      expect(store.getActions()).toEqual([
        requestMeters.request(existingPage),
        requestMeters.success({...normalizedMeterResponse(existingPage)}),
        paginationUpdateMetaData({model: 'meters', ...normalizedMeterResponse(existingPage).result}),
      ]);
    });

    it('request a get to an already fetched page with a result list', async () => {
      const existingPage = 1;
      const initialState: Partial<RootState> = {
        paginatedDomainModels: {
          meters: {entities: {}, result: {[existingPage]: {isFetching: false, result: [1]}}},
        },
      };
      store = configureMockStore(initialState);

      await getMetersWithResponseOk(existingPage);

      expect(store.getActions()).toEqual([]);
    });
  });
});

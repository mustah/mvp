import axios from 'axios';
import {normalize} from 'normalizr';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {makeMeterDto, MeterDto} from '../../../../__tests__/testDataFactory';
import {initLanguage} from '../../../../i18n/i18n';
import {RootState} from '../../../../reducers/rootReducer';
import {EndPoints} from '../../../../services/endPoints';
import {authenticate} from '../../../../services/restClient';
import {ErrorResponse} from '../../../../types/Types';
import {paginationUpdateMetaData} from '../../../ui/pagination/paginationActions';
import {HasPageNumber, NormalizedPaginated} from '../../paginatedDomainModels';
import {domainModelPaginatedClearError, getRequestOf} from '../../paginatedDomainModelsActions';
import {clearErrorMeters, fetchMeters} from '../meterApiActions';
import {Meter} from '../meterModels';
import {meterSchema} from '../meterSchema';
import MockAdapter = require('axios-mock-adapter');

initLanguage({code: 'en', name: 'english'});
const configureMockStore = configureStore([thunk]);
let store;
let mockRestClient;

describe('meterApiActions', () => {

  const initialRootState: Partial<RootState> = {
    paginatedDomainModels: {
      meters: {isFetchingSingle: false, nonExistingSingles: {}, entities: {}, result: {}},
      gateways: {isFetchingSingle: false, nonExistingSingles: {}, entities: {}, result: {}},
    },
  };

  const requestMeters = getRequestOf<NormalizedPaginated<Meter>>(EndPoints.meters);

  beforeEach(() => {
    store = configureMockStore(initialRootState);
    mockRestClient = new MockAdapter(axios);
    authenticate('test');
  });

  afterEach(() => {
    mockRestClient.reset();
  });

  describe('fetch meters from /meters', () => {
    const page = 0;
    const meter1: MeterDto = makeMeterDto(
      1,
      {id: 1, name: 'gothenburg'},
      {id: 1, name: 'kungsgatan'},
    );
    const meter2: MeterDto = makeMeterDto(
      2,
      {id: 2, name: 'stockholm'},
      {id: 2, name: 'kungsgatan'},
    );

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
        paginationUpdateMetaData({entityType: 'meters', ...normalizedMeterResponse(page).result}),
      ]);
    });

    it('get an error message back on a bad request with no response from backend', async () => {
      await getMetersWithBadRequest(page, undefined);

      expect(store.getActions()).toEqual([
        requestMeters.request(page),
        requestMeters.failure({page, message: 'An unexpected error occurred'}),
      ]);
    });

    it('get an error message back on a bad request and response from backend', async () => {
      const errorResponse: ErrorResponse = {message: 'an error'};

      await getMetersWithBadRequest(page, errorResponse);

      expect(store.getActions()).toEqual([
        requestMeters.request(page),
        requestMeters.failure({page, ...errorResponse}),
      ]);
    });

    it('dont fetch data if already existing', async () => {
      const existingPage = 1;
      const initialState: Partial<RootState> = {
        paginatedDomainModels: {
          meters: {
            isFetchingSingle: false,
            nonExistingSingles: {},
            entities: {},
            result: {[existingPage]: {isFetching: false, isSuccessfullyFetched: true, result: []}},
          },
          gateways: {
            isFetchingSingle: false,
            nonExistingSingles: {},
            entities: {},
            result: {},
          },
        },
      };
      store = configureMockStore(initialState);

      await getMetersWithResponseOk(existingPage);

      expect(store.getActions()).toEqual([]);
    });

    it('dont fetch data if already fetching', async () => {
      const existingPage = 1;
      const initialState: Partial<RootState> = {
        paginatedDomainModels: {
          meters: {
            isFetchingSingle: false,
            nonExistingSingles: {},
            entities: {},
            result: {[existingPage]: {isFetching: true, isSuccessfullyFetched: false}},
          },
          gateways: {
            isFetchingSingle: false,
            nonExistingSingles: {},
            entities: {},
            result: {},
          },
        },
      };
      store = configureMockStore(initialState);

      await getMetersWithResponseOk(existingPage);

      expect(store.getActions()).toEqual([]);
    });

    it('dont fetch data if received an error', async () => {
      const existingPage = 1;
      const initialState: Partial<RootState> = {
        paginatedDomainModels: {
          meters: {
            isFetchingSingle: false,
            nonExistingSingles: {},
            entities: {},
            result: {
              [existingPage]: {
                isFetching: false,
                isSuccessfullyFetched: false,
                error: {message: 'an error'},
              },
            },
          },
          gateways: {
            isFetchingSingle: false,
            nonExistingSingles: {},
            entities: {},
            result: {},
          },
        },
      };
      store = configureMockStore(initialState);

      await getMetersWithResponseOk(existingPage);

      expect(store.getActions()).toEqual([]);
    });

  });

  describe('clear error', () => {
    it('send a request to clear error of a specified page', () => {
      const payload: HasPageNumber = {page: 1};
      store.dispatch(clearErrorMeters(payload));

      expect(store.getActions()).toEqual([
        {type: domainModelPaginatedClearError(EndPoints.meters), payload},
      ]);
    });
  });
});

import axios from 'axios';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {makeMeterDto, MeterDto} from '../../../../__tests__/testDataFactory';
import {initTranslations} from '../../../../i18n/__tests__/i18nMock';
import {RootState} from '../../../../reducers/rootReducer';
import {EndPoints} from '../../../../services/endPoints';
import {authenticate} from '../../../../services/restClient';
import {ErrorResponse, uuid} from '../../../../types/Types';
import {noInternetConnection, requestTimeout} from '../../../api/apiActions';
import {updatePageMetaData} from '../../../ui/pagination/paginationActions';
import {NormalizedPaginated, PageNumbered} from '../../paginatedDomainModels';
import {
  domainModelPaginatedClearError,
  makeRequestActionsOf,
} from '../../paginatedDomainModelsActions';
import {makeEntityRequestActionsOf} from '../../paginatedDomainModelsEntityActions';
import {makeInitialState} from '../../paginatedDomainModelsReducer';
import {clearErrorMeters, fetchMeterEntities, fetchMeters} from '../meterApiActions';
import {Meter, MetersState} from '../meterModels';
import {meterDataFormatter} from '../meterSchema';
import MockAdapter = require('axios-mock-adapter');

describe('meterApiActions', () => {

  initTranslations({
    code: 'en',
    translation: {
      test: 'no translations will default to key',
    },
  });

  const configureMockStore = configureStore([thunk]);
  let store;
  let mockRestClient;

  const initialRootState: Partial<RootState> = {
    paginatedDomainModels: {
      meters: {isFetchingSingle: false, nonExistingSingles: {}, entities: {}, result: {}},
      gateways: {isFetchingSingle: false, nonExistingSingles: {}, entities: {}, result: {}},
    },
  };

  const requestMeters = makeRequestActionsOf<NormalizedPaginated<Meter>>(EndPoints.meters);

  beforeEach(() => {
    store = configureMockStore(initialRootState);
    mockRestClient = new MockAdapter(axios);
    authenticate('test');
  });

  afterEach(() => {
    mockRestClient.reset();
  });

  describe('fetchMeters', () => {
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
      ...meterDataFormatter(meterResponse),
    });

    it('normalizes data on successful request', async () => {

      await getMetersWithResponseOk(page);

      expect(store.getActions()).toEqual([
        requestMeters.request(page),
        requestMeters.success({...normalizedMeterResponse(page)}),
        updatePageMetaData({entityType: 'meters', ...normalizedMeterResponse(page).result}),
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

    it('do not fetch data if already existing', async () => {
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

    it('do not fetch data if already fetching', async () => {
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

    it('do not fetch data if received an error', async () => {
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

    describe('network error', () => {

      it('display error message when there is not internet connection', async () => {
        const fetchMetersWhenOffline = async (page: number) => {
          mockRestClient.onGet(EndPoints.meters).networkError();
          return store.dispatch(fetchMeters(page));
        };

        await fetchMetersWhenOffline(1);

        expect(store.getActions()).toEqual([
          requestMeters.request(1),
          requestMeters.failure({...noInternetConnection(), page: 1}),
        ]);
      });
    });

    describe('request timeout', () => {

      it('display error message when the request times out', async () => {
        const fetchMetersAndTimeout = async (page: number) => {
          mockRestClient.onGet(EndPoints.meters).timeout();
          return store.dispatch(fetchMeters(page));
        };

        await fetchMetersAndTimeout(1);

        expect(store.getActions()).toEqual([
          requestMeters.request(1),
          requestMeters.failure({...requestTimeout(), page: 1}),
        ]);
      });
    });

  });

  describe('fetchMeterEntities', () => {
    const fetchMeterEntitiesRequest = makeEntityRequestActionsOf<Meter[]>(EndPoints.meters);

    const meter1: Partial<Meter> = {id: 1};
    const meter2: Partial<Meter> = {id: 2};
    const meter3: Partial<Meter> = {id: 3};
    const meters: Array<Partial<Meter>> = [
      meter1,
      meter2,
      meter3,
    ];

    const meterIds: uuid[] = [1, 2, 3];

    const fetchMeterEntitiesWithResponseOk = async (ids: uuid[]) => {
      mockRestClient
        .onGet(`${EndPoints.meters}?id=${meterIds[0]}&id=${meterIds[1]}&id=${meterIds[2]}`)
        .reply(201, {content: meters});

      return store.dispatch(fetchMeterEntities(ids));
    };

    it('does not normalize data', async () => {
      await fetchMeterEntitiesWithResponseOk(meterIds);

      expect(store.getActions()).toEqual([
        fetchMeterEntitiesRequest.request(),
        fetchMeterEntitiesRequest.success(meters as Meter[]),
      ]);
    });

    it('does not fetch if already fetching', async () => {
      const initialState: MetersState = {...makeInitialState(), isFetchingSingle: true};
      store = configureMockStore({paginatedDomainModels: {meters: initialState}});

      await fetchMeterEntitiesWithResponseOk(meterIds);

      expect(store.getActions()).toEqual([]);
    });

    it('fetches even if a fraction of the requested entities already exist in state', async () => {
      const initialState: MetersState = {
        ...makeInitialState(),
        entities: {1: meter1 as Meter, 2: meter2 as Meter},
      };
      store = configureMockStore({paginatedDomainModels: {meters: initialState}});

      await fetchMeterEntitiesWithResponseOk(meterIds);

      expect(store.getActions()).toEqual([
        fetchMeterEntitiesRequest.request(),
        fetchMeterEntitiesRequest.success(meters as Meter[]),
      ]);
    });

    it('does not fetch is all entities already exist in state', async () => {
      const initialState: MetersState = {
        ...makeInitialState(),
        entities: {1: meter1 as Meter, 2: meter2 as Meter, 3: meter3 as Meter},
      };
      store = configureMockStore({paginatedDomainModels: {meters: initialState}});

      await fetchMeterEntitiesWithResponseOk(meterIds);

      expect(store.getActions()).toEqual([]);
    });

    describe('network error', () => {

      it('display error message when there is not internet connection', async () => {
        const fetchMetersWhenOffline = async () => {
          mockRestClient
            .onGet(`${EndPoints.meters}?id=${meterIds[0]}&id=${meterIds[1]}&id=${meterIds[2]}`)
            .networkError();
          return store.dispatch(fetchMeterEntities(meterIds));
        };

        await fetchMetersWhenOffline();

        expect(store.getActions()).toEqual([
          fetchMeterEntitiesRequest.request(),
          fetchMeterEntitiesRequest.failure({id: -1, ...noInternetConnection()}),
        ]);
      });
    });

  });

  describe('clear error', () => {

    it('send a request to clear error of a specified page', () => {
      const payload: PageNumbered = {page: 1};
      store.dispatch(clearErrorMeters(payload));

      expect(store.getActions()).toEqual([
        {type: domainModelPaginatedClearError(EndPoints.meters), payload},
      ]);
    });
  });

});

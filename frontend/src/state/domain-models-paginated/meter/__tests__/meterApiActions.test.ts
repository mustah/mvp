import axios from 'axios';
import {normalize} from 'normalizr';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {makeMeterDto, MeterDto} from '../../../../__tests__/testDataFactory';
import {initTranslations} from '../../../../i18n/__tests__/i18nMock';
import {RootState} from '../../../../reducers/rootReducer';
import {EndPoints} from '../../../../services/endPoints';
import {authenticate} from '../../../../services/restClient';
import {ErrorResponse, uuid} from '../../../../types/Types';
import {paginationUpdateMetaData} from '../../../ui/pagination/paginationActions';
import {HasPageNumber, NormalizedPaginated} from '../../paginatedDomainModels';
import {domainModelPaginatedClearError, makeRequestActionsOf} from '../../paginatedDomainModelsActions';
import {makeEntityRequestActionsOf} from '../../paginatedDomainModelsEntityActions';
import {initialPaginatedDomain} from '../../paginatedDomainModelsReducer';
import {clearErrorMeters, fetchMeter, fetchMeterEntities, fetchMeters} from '../meterApiActions';
import {Meter, MetersState} from '../meterModels';
import {meterSchema} from '../meterSchema';
import MockAdapter = require('axios-mock-adapter');

initTranslations({
  code: 'en',
  translation: {
    test: 'no translations will default to key',
  },
});
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

  describe('fetchMeter', () => {
    const getMeterRequest = makeEntityRequestActionsOf<Meter[]>(EndPoints.meters);
    const meter: Partial<Meter> = {
      id: 1,
      flags: [],
    };

    const fetchMeterWithResponseOk = async (id: uuid) => {
      mockRestClient.onGet(`${EndPoints.meters}/${id.toString()}`).reply(201, meter);
      return store.dispatch(fetchMeter(id));
    };

    it('does not normalize response', async () => {
      await fetchMeterWithResponseOk(meter.id as uuid);

      expect(store.getActions()).toEqual([
        getMeterRequest.request(),
        getMeterRequest.success(meter as Meter[]),
      ]);
    });

    it('does not fetch if already fetching entity', async () => {
      const initialState: MetersState = {...initialPaginatedDomain(), isFetchingSingle: true};
      store = configureMockStore({paginatedDomainModels: {meters: initialState}});

      await fetchMeterWithResponseOk(meter.id as uuid);

      expect(store.getActions()).toEqual([]);
    });

    it('does not fetch is entity already exist in state', async () => {
      const initialState: MetersState = {...initialPaginatedDomain(), entities: {1: meter as Meter}};
      store = configureMockStore({paginatedDomainModels: {meters: initialState}});

      await fetchMeterWithResponseOk(meter.id as uuid);

      expect(store.getActions()).toEqual([]);
    });

    it('does not fetch if entity have been attempted to be fetched but failed', async () => {
      const initialState: MetersState = {
        ...initialPaginatedDomain(),
        nonExistingSingles: {1: {id: 1, message: 'meter does not exist'}},
      };
      store = configureMockStore({paginatedDomainModels: {meters: initialState}});

      await fetchMeterWithResponseOk(meter.id as uuid);

      expect(store.getActions()).toEqual([]);
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
      const initialState: MetersState = {...initialPaginatedDomain(), isFetchingSingle: true};
      store = configureMockStore({paginatedDomainModels: {meters: initialState}});

      await fetchMeterEntitiesWithResponseOk(meterIds);

      expect(store.getActions()).toEqual([]);
    });

    it('fetches even if a fraction of the requested entities already exist in state', async () => {
      const initialState: MetersState = {
        ...initialPaginatedDomain(),
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
        ...initialPaginatedDomain(),
        entities: {1: meter1 as Meter, 2: meter2 as Meter, 3: meter3 as Meter},
      };
      store = configureMockStore({paginatedDomainModels: {meters: initialState}});

      await fetchMeterEntitiesWithResponseOk(meterIds);

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

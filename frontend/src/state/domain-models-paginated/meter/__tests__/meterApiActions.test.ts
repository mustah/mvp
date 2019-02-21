import axios from 'axios';
import {default as MockAdapter} from 'axios-mock-adapter';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {makeMeterDto, MeterDto} from '../../../../__tests__/testDataFactory';
import {initTranslations} from '../../../../i18n/__tests__/i18nMock';
import {RootState} from '../../../../reducers/rootReducer';
import {EndPoints} from '../../../../services/endPoints';
import {authenticate} from '../../../../services/restClient';
import {ErrorResponse} from '../../../../types/Types';
import {noInternetConnection, requestTimeout} from '../../../api/apiActions';
import {showFailMessage, showSuccessMessage} from '../../../ui/message/messageActions';
import {updatePageMetaData} from '../../../ui/pagination/paginationActions';
import {NormalizedPaginated, PageNumbered} from '../../paginatedDomainModels';
import {domainModelPaginatedClearError, makeRequestActionsOf} from '../../paginatedDomainModelsActions';
import {
  domainModelsPaginatedDeleteFailure,
  domainModelsPaginatedDeleteRequest,
  domainModelsPaginatedDeleteSuccess
} from '../../paginatedDomainModelsEntityActions';
import {clearErrorMeters, deleteMeter, fetchMeters} from '../meterApiActions';
import {Meter} from '../meterModels';
import {meterDataFormatter} from '../meterSchema';

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
    const meter1: MeterDto = makeMeterDto(1, 'gothenburg', 'kungsgatan');
    const meter2: MeterDto = makeMeterDto(2, 'stockholm', 'kungsgatan');

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

  describe('clear error', () => {

    it('send a request to clear error of a specified page', () => {
      const payload: PageNumbered = {page: 1};
      store.dispatch(clearErrorMeters(payload));

      expect(store.getActions()).toEqual([
        {type: domainModelPaginatedClearError(EndPoints.meters), payload},
      ]);
    });
  });

  describe('deleteMeter', () => {

    it('cannot delete meter when request times out', async () => {
      const deleteMeterWithTimeout = async (id: number) => {
        mockRestClient.onDelete(`${EndPoints.meters}/${id}`).timeout();
        return store.dispatch(deleteMeter(id, 0));
      };

      await deleteMeterWithTimeout(1);

      expect(store.getActions()).toEqual([
        {type: domainModelsPaginatedDeleteRequest(EndPoints.meters)},
        {
          type: domainModelsPaginatedDeleteFailure(EndPoints.meters),
          payload: {id: 1, message: 'Looks like the server is taking to long to respond, please try again in soon'}
        },
      ]);
    });

    it('has error response when deleting meter', async () => {
      const payload = {message: 'my bad', id: 1};

      const deleteMeterWithTimeout = async (id: number) => {
        mockRestClient.onDelete(`${EndPoints.meters}/${id}`).reply(401, payload);
        return store.dispatch(deleteMeter(id, 1));
      };

      await deleteMeterWithTimeout(payload.id);

      expect(store.getActions()).toEqual([
        {type: domainModelsPaginatedDeleteRequest(EndPoints.meters)},
        {
          type: domainModelsPaginatedDeleteFailure(EndPoints.meters),
          payload
        },
        showFailMessage('Failed to delete the meter: my bad')
      ]);
    });

    it('deletes meter successfully', async () => {
      const page = 2;
      const payload = {id: 1, facility: `demo-1`, page};

      const deleteMeterWithTimeout = async (id: number, page: number) => {
        mockRestClient.onDelete(`${EndPoints.meters}/${id}`).reply(200, payload);
        return store.dispatch(deleteMeter(id, page));
      };

      await deleteMeterWithTimeout(1, page);

      expect(store.getActions()).toEqual([
        {type: domainModelsPaginatedDeleteRequest(EndPoints.meters)},
        {
          type: domainModelsPaginatedDeleteSuccess(EndPoints.meters),
          payload,
        },
        showSuccessMessage('Successfully deleted the meter demo-1')
      ]);
    });
  });

});

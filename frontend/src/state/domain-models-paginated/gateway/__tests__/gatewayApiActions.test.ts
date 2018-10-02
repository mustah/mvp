import axios from 'axios';
import {default as MockAdapter} from 'axios-mock-adapter';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {testData} from '../../../../__tests__/testDataFactory';
import {initTranslations} from '../../../../i18n/__tests__/i18nMock';
import {EndPoints} from '../../../../services/endPoints';
import {authenticate} from '../../../../services/restClient';
import {uuid} from '../../../../types/Types';
import {requestTimeout} from '../../../api/apiActions';
import {updatePageMetaData} from '../../../ui/pagination/paginationActions';
import {NormalizedPaginated, PaginatedDomainModelsState} from '../../paginatedDomainModels';
import {domainModelPaginatedClearError, makeRequestActionsOf} from '../../paginatedDomainModelsActions';
import {makeEntityRequestActionsOf} from '../../paginatedDomainModelsEntityActions';
import {makeInitialState} from '../../paginatedDomainModelsReducer';
import {clearErrorGateways, fetchGateway, fetchGateways} from '../gatewayApiActions';
import {Gateway, GatewaysState} from '../gatewayModels';
import {gatewayDataFormatter} from '../gatewaySchema';

const configureMockStore = configureStore([thunk]);

describe('gatewayApiActions', () => {

  initTranslations({
    code: 'en',
    translation: {
      test: 'no translations will default to key',
    },
  });

  let mockRestClient: MockAdapter;
  let store;

  const getGateways = makeRequestActionsOf<NormalizedPaginated<Gateway>>(EndPoints.gateways);

  beforeEach(() => {
    const initialState: Partial<PaginatedDomainModelsState> = {
      gateways: {...makeInitialState()},
    };
    store = configureMockStore({paginatedDomainModels: initialState});
    mockRestClient = new MockAdapter(axios);
    authenticate('test');
  });

  afterEach(() => {
    mockRestClient.reset();
  });

  describe('fetchGateways', () => {
    const page = 0;

    const getGatewaysWithResponseOk = async () => {
      mockRestClient.onGet(EndPoints.gateways).reply(200, testData.gateways);
      return store.dispatch(fetchGateways(page));
    };

    it('normalizes data and updates pagination metaData', async () => {
      await getGatewaysWithResponseOk();

      expect(store.getActions()).toEqual([
        getGateways.request(page),
        getGateways.success({...gatewayDataFormatter(testData.gateways), page}),
        updatePageMetaData({
          entityType: 'gateways',
          content: ['g1', 'g2', 'g3', 'g4', 'g5'],
          totalElements: 5,
          totalPages: 1,
          first: true,
          last: true,
          number: 0,
          numberOfElements: 5,
          size: 20,
          sort: null,
        }),
      ]);
    });
  });

  describe('fetchGateway', () => {
    const requests = makeEntityRequestActionsOf<Gateway>(EndPoints.gateways);
    const gateway: Partial<Gateway> = {
      id: 1,
      meterIds: [1, 2, 3],
    };

    const fetchGatewayWithResponseOk = async (id: uuid) => {
      mockRestClient.onGet(`${EndPoints.gateways}/${id}`).reply(201, gateway);
      return store.dispatch(fetchGateway(id));
    };

    it('does not normalize data', async () => {
      await fetchGatewayWithResponseOk(gateway.id as uuid);

      expect(store.getActions()).toEqual([
        requests.request(),
        requests.success(gateway as Gateway),
      ]);
    });

    it('does not fetch data if already fetching an entity', async () => {
      const initialState: Partial<GatewaysState> = {...makeInitialState(), isFetchingSingle: true};
      store = configureMockStore({paginatedDomainModels: {gateways: initialState}});

      await fetchGatewayWithResponseOk(gateway.id as uuid);

      expect(store.getActions()).toEqual([]);
    });

    it('does not fetch if entity already already exists in state', async () => {
      const initialState: Partial<GatewaysState> = {
        ...makeInitialState(),
        entities: {1: gateway as Gateway},
      };
      store = configureMockStore({paginatedDomainModels: {gateways: initialState}});

      await fetchGatewayWithResponseOk(gateway.id as uuid);

      expect(store.getActions()).toEqual([]);
    });

    it('does not fetch if entity already has been been fetched, but failed', async () => {
        const initialState: Partial<GatewaysState> = {
          ...makeInitialState(),
          nonExistingSingles: {1: {id: 1, message: 'gateway not found'}},
        };
        store = configureMockStore({paginatedDomainModels: {gateways: initialState}});

        await fetchGatewayWithResponseOk(gateway.id as uuid);

        expect(store.getActions()).toEqual([]);
      },
    );

    describe('request timeout', () => {

      it('display error message when the request times out', async () => {
        const fetchMetersAndTimeout = async () => {
          mockRestClient.onGet(`${EndPoints.gateways}/1`).timeout();
          return store.dispatch(fetchGateway(1));
        };

        await fetchMetersAndTimeout();

        expect(store.getActions()).toEqual([
          requests.request(),
          requests.failure({id: 1, ...requestTimeout()}),
        ]);
      });
    });

  });

  describe('clear error', () => {

    it('dispatches a clear error action', () => {
      const page = 0;
      store.dispatch(clearErrorGateways({page}));

      expect(store.getActions()).toEqual([
        {type: domainModelPaginatedClearError(EndPoints.gateways), payload: {page}},
      ]);
    });
  });
});

import axios from 'axios';
import {normalize} from 'normalizr';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {testData} from '../../../../__tests__/testDataFactory';
import {initLanguage} from '../../../../i18n/i18n';
import {EndPoints} from '../../../../services/endPoints';
import {authenticate} from '../../../../services/restClient';
import {uuid} from '../../../../types/Types';
import {paginationUpdateMetaData} from '../../../ui/pagination/paginationActions';
import {NormalizedPaginated, PaginatedDomainModelsState} from '../../paginatedDomainModels';
import {domainModelPaginatedClearError, getRequestOf} from '../../paginatedDomainModelsActions';
import {getRequestEntityOf} from '../../paginatedDomainModelsEntityActions';
import {initialPaginatedDomain} from '../../paginatedDomainModelsReducer';
import {clearErrorGateways, fetchGateway, fetchGateways} from '../gatewayApiActions';
import {Gateway, GatewaysState} from '../gatewayModels';
import {gatewaySchema} from '../gatewaySchema';
import MockAdapter = require('axios-mock-adapter');

const configureMockStore = configureStore([thunk]);

describe('gatewayApiActions', () => {

  initLanguage({code: 'en', name: 'english'});

  let mockRestClient: MockAdapter;
  let store;

  const getGateways = getRequestOf<NormalizedPaginated<Gateway>>(EndPoints.gateways);

  beforeEach(() => {
    const initialState: Partial<PaginatedDomainModelsState> = {
      gateways: {...initialPaginatedDomain()},
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
        getGateways.success({...normalize(testData.gateways, gatewaySchema), page}),
        paginationUpdateMetaData({
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
    const gatewayEntityRequest = getRequestEntityOf<Gateway>(EndPoints.gateways);
    const gateway: Partial<Gateway> = {
      id: 1,
      meterIds: [1, 2, 3],
    };
    const fetchGatewayWithResponseOk = async (id: uuid) => {
      mockRestClient.onGet(`${EndPoints.gateways}/${id.toString()}`).reply(201, gateway);
      return store.dispatch(fetchGateway(id));
    };

    it('does not normalize data', async () => {
      await fetchGatewayWithResponseOk(gateway.id as uuid);

      expect(store.getActions()).toEqual([
        gatewayEntityRequest.request(),
        gatewayEntityRequest.success(gateway as Gateway),
      ]);
    });
    it('does not fetch data if already fetching an entity', async () => {
      const initialState: Partial<GatewaysState> = {...initialPaginatedDomain(), isFetchingSingle: true};
      store = configureMockStore({paginatedDomainModels: {gateways: initialState}});

      await fetchGatewayWithResponseOk(gateway.id as uuid);

      expect(store.getActions()).toEqual([]);

    });

    it('does not fetch if entity already already exists in state', async () => {
      const initialState: Partial<GatewaysState> = {...initialPaginatedDomain(), entities: {1: gateway as Gateway}};
      store = configureMockStore({paginatedDomainModels: {gateways: initialState}});

      await fetchGatewayWithResponseOk(gateway.id as uuid);

      expect(store.getActions()).toEqual([]);
    });

    it('does not fetch if entity already have been attempted to be fetched but failed', async () => {
      const initialState: Partial<GatewaysState> = {
        ...initialPaginatedDomain(),
        nonExistingSingles: {1: {id: 1, message: 'gateway not found'}},
      };
      store = configureMockStore({paginatedDomainModels: {gateways: initialState}});

      await fetchGatewayWithResponseOk(gateway.id as uuid);

      expect(store.getActions()).toEqual([]);
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

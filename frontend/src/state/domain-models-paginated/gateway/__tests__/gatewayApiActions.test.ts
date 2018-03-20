import axios from 'axios';
import {normalize} from 'normalizr';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {testData} from '../../../../__tests__/testDataFactory';
import {initLanguage} from '../../../../i18n/i18n';
import {EndPoints} from '../../../../services/endPoints';
import {authenticate} from '../../../../services/restClient';
import {paginationUpdateMetaData} from '../../../ui/pagination/paginationActions';
import {NormalizedPaginated, PaginatedDomainModelsState} from '../../paginatedDomainModels';
import {domainModelPaginatedClearError, getRequestOf} from '../../paginatedDomainModelsActions';
import {initialPaginatedDomain} from '../../paginatedDomainModelsReducer';
import {clearErrorGateways, fetchGateways} from '../gatewayApiActions';
import {Gateway} from '../gatewayModels';
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

  describe('get gateways', () => {

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

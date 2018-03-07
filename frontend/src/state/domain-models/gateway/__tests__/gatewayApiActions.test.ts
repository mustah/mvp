import axios from 'axios';
import {normalize} from 'normalizr';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {testData} from '../../../../__tests__/testDataFactory';
import {initLanguage} from '../../../../i18n/i18n';
import {EndPoints} from '../../../../services/endPoints';
import {authenticate} from '../../../../services/restClient';
import {paginationUpdateMetaData} from '../../../ui/pagination/paginationActions';
import {limit} from '../../../ui/pagination/paginationReducer';
import {DomainModelsState, Normalized} from '../../domainModels';
import {domainModelsClearError, getRequestOf} from '../../domainModelsActions';
import {initialDomain} from '../../domainModelsReducer';
import {clearErrorGateways, fetchGateways} from '../gatewayApiActions';
import {Gateway} from '../gatewayModels';
import {gatewaySchema} from '../gatewaySchema';
import MockAdapter = require('axios-mock-adapter');

const configureMockStore = configureStore([thunk]);

describe('gatewayApiActions', () => {

  initLanguage({code: 'en', name: 'english'});

  let mockRestClient: MockAdapter;
  let store;

  const getGateways = getRequestOf<Normalized<Gateway>>(EndPoints.gateways);

  beforeEach(() => {
    const initialState: Partial<DomainModelsState> = {
      gateways: {...initialDomain()},
    };
    store = configureMockStore({domainModels: initialState});
    mockRestClient = new MockAdapter(axios);
    authenticate('test');
  });

  afterEach(() => {
    mockRestClient.reset();
  });

  describe('get gateways', () => {

    const getGatewaysWithResponseOk = async () => {
      mockRestClient.onGet(EndPoints.gateways).reply(200, testData.gateways);
      return store.dispatch(fetchGateways());
    };

    it('normalizes data and updates pagination metaData', async () => {
      await getGatewaysWithResponseOk();

      expect(store.getActions()).toEqual([
        getGateways.request(),
        getGateways.success(normalize(testData.gateways, gatewaySchema)),
        paginationUpdateMetaData({
          entityType: 'gateways',
          content: ['g1', 'g2', 'g3', 'g4', 'g5'],
          totalElements: 5,
          totalPages: Math.ceil(5 / limit),
        }),
      ]);
    });
  });

  describe('clear error', () => {
    it('dispatches a clear error action', () => {
      store.dispatch(clearErrorGateways());

      expect(store.getActions()).toEqual([
        {type: domainModelsClearError(EndPoints.gateways)},
      ]);
    });
  });
});

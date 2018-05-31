import axios from 'axios';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {testData} from '../../../../__tests__/testDataFactory';
import {initTranslations} from '../../../../i18n/__tests__/i18nMock';
import {EndPoints} from '../../../../services/endPoints';
import {authenticate} from '../../../../services/restClient';
import {IdNamed} from '../../../../types/Types';
import {DomainModelsState, Normalized} from '../../domainModels';
import {getRequestOf} from '../../domainModelsActions';
import {initialDomain} from '../../domainModelsReducer';
import {fetchSelections} from '../selectionsApiActions';
import {selectionsDataFormatter} from '../selectionsSchemas';
import MockAdapter = require('axios-mock-adapter');

const configureMockStore = configureStore([thunk]);

describe('selectionApiActions', () => {
  initTranslations({
    code: 'en',
    translation: {
      test: 'no translations will default to key',
    },
  });
  let mockRestClient: MockAdapter;
  let store;
  const selectionsRequest = getRequestOf<Normalized<IdNamed>>(EndPoints.selections);

  beforeEach(() => {
    const initialState: Partial<DomainModelsState> = {
      cities: {...initialDomain()},
    };
    store = configureMockStore({domainModels: initialState});
    mockRestClient = new MockAdapter(axios);
    authenticate('test');
  });

  afterEach(() => {
    mockRestClient.reset();
  });

  describe('fetch domainModels from /selections', () => {

    const getSelectionWithResponseOk = async () => {
      mockRestClient.onGet(EndPoints.selections).reply(200, testData.selections);
      return store.dispatch(fetchSelections());
    };

    const getSelectionWithBadRequest = async (response) => {
      mockRestClient.onGet(EndPoints.selections).reply(401, response);
      return store.dispatch(fetchSelections());
    };

    it('normalizes data', async () => {
      await getSelectionWithResponseOk();

      expect(store.getActions()).toEqual([
        selectionsRequest.request(),
        selectionsRequest.success(selectionsDataFormatter(testData.selections)),
      ]);
    });

    it('throws exception when no data exists', async () => {
      const response = {message: 'failed'};

      await getSelectionWithBadRequest(response);

      expect(store.getActions()).toEqual([
        selectionsRequest.request(),
        selectionsRequest.failure({...response}),
      ]);
    });
    it('does not fetch data if it already exists', async () => {
      const fetchedState: Partial<DomainModelsState> = {
        cities: {
          isFetching: false,
          isSuccessfullyFetched: true,
          total: 1,
          entities: {1: {id: 1, name: '1'}},
          result: [1],
        },
      };
      store = configureMockStore({domainModels: {...fetchedState}});

      await getSelectionWithResponseOk();

      expect(store.getActions()).toEqual([]);
    });
    it('does not fetch data if already fetching', async () => {
      const fetchedState: Partial<DomainModelsState> = {
        cities: {
          isFetching: true,
          isSuccessfullyFetched: false,
          total: 0,
          entities: {},
          result: [1, 2],
        },
      };

      store = configureMockStore({domainModels: {...fetchedState}});

      await getSelectionWithResponseOk();

      expect(store.getActions()).toEqual([]);
    });
    it('does not fetch data if received an error', async () => {
      const fetchedState: Partial<DomainModelsState> = {
        cities: {
          isFetching: false,
          isSuccessfullyFetched: false,
          total: 0,
          entities: {},
          result: [1, 2],
          error: {message: 'an error'},
        },
      };

      store = configureMockStore({domainModels: {...fetchedState}});

      await getSelectionWithResponseOk();

      expect(store.getActions()).toEqual([]);
    });
  });
});

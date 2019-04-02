import axios from 'axios';
import {default as MockAdapter} from 'axios-mock-adapter';
import {routerActions} from 'react-router-redux';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {routes} from '../../../app/routes';
import {InvalidToken} from '../../../exceptions/InvalidToken';
import {initTranslations} from '../../../i18n/__tests__/i18nMock';
import {RootState} from '../../../reducers/rootReducer';
import {EndPoints} from '../../../services/endPoints';
import {restClientWith} from '../../../services/restClient';
import {logoutUser} from '../../../usecases/auth/authActions';
import {Unauthorized} from '../../../usecases/auth/authModels';
import {makeActionsOf, noInternetConnection, requestTimeout} from '../../api/apiActions';
import {User} from '../../domain-models/user/userModels';
import {Medium} from '../../ui/graph/measurement/measurementModels';
import {fetchSelectionTree} from '../selectionTreeApiActions';
import {NormalizedSelectionTree} from '../selectionTreeModels';
import {initialState as initialSelectionTreeState} from '../selectionTreeReducer';
import {selectionTreeDataFormatter} from '../selectionTreeSchemas';

describe('selectionTreeApiActions', () => {
  initTranslations({
    code: 'en',
    translation: {
      test: 'no translations will default to key',
    },
  });

  const configureMockStore = configureStore([thunk]);
  let store;
  let mockRestClient;

  beforeEach(() => {
    const initialState: Partial<RootState> = {
      selectionTree: {...initialSelectionTreeState},
    };
    store = configureMockStore({...initialState});
    mockRestClient = new MockAdapter(axios);
    restClientWith('123123123');
  });

  afterEach(() => {
    mockRestClient.reset();
  });

  const responseFromApi = {
    cities: [
      {
        id: 'sweden,kungsbacka',
        name: 'kungsbacka',
        medium: ['District heating'],
        addresses: [
          {
            name: 'kabelgatan 1',
            meters: [
              {
                id: 1,
                medium: 'District heating',
                name: 'extId1',
              },
              {
                id: 2,
                medium: 'District heating',
                name: 'extId2',
              },
            ],
          },
          {
            name: 'kungsgatan 42',
            meters: [
              {
                id: 5,
                medium: 'District heating',
                name: 'extId5',
              },
              {
                id: 6,
                medium: 'District heating',
                name: 'extId6',
              },
            ],
          },
        ],
      },
      {
        id: 'sweden,gothenburg',
        name: 'gothenburg',
        medium: ['District heating'],
        addresses: [
          {
            name: 'kungsgatan 42',
            meters: [
              {
                id: 3,
                medium: 'District heating',
                name: 'extId3',
              },
              {
                id: 4,
                medium: 'District heating',
                name: 'extId4',
              },
            ],
          },
        ],
      },
    ],
  };

  const normalizedResponse: NormalizedSelectionTree = selectionTreeDataFormatter(responseFromApi);

  const actions = makeActionsOf<NormalizedSelectionTree>(EndPoints.selectionTree);

  const getSelectionTreeWithResponseOk = async () => {
    mockRestClient.onGet(EndPoints.selectionTree).reply(201, responseFromApi);
    return store.dispatch(fetchSelectionTree());
  };

  const getSelectionTreeWithBadRequest = async () => {
    mockRestClient.onGet(EndPoints.selectionTree).reply(401, {message: 'an error'});
    return store.dispatch(fetchSelectionTree());
  };

  it('normalizes data', () => {
    const expected = {
      entities: {
        addresses: {
          'sweden,kungsbacka,kabelgatan 1': {
            city: 'sweden,kungsbacka',
            id: 'sweden,kungsbacka,kabelgatan 1',
            name: 'kabelgatan 1',
            meters: [1, 2],
          },
          'sweden,kungsbacka,kungsgatan 42': {
            city: 'sweden,kungsbacka',
            id: 'sweden,kungsbacka,kungsgatan 42',
            name: 'kungsgatan 42',
            meters: [5, 6],
          },
          'sweden,gothenburg,kungsgatan 42': {
            city: 'sweden,gothenburg',
            id: 'sweden,gothenburg,kungsgatan 42',
            name: 'kungsgatan 42',
            meters: [3, 4],
          },
        },
        cities: {
          'sweden,gothenburg': {
            id: 'sweden,gothenburg',
            medium: [Medium.districtHeating],
            name: 'gothenburg',
            addresses: ['sweden,gothenburg,kungsgatan 42'],
          },
          'sweden,kungsbacka': {
            id: 'sweden,kungsbacka',
            medium: [Medium.districtHeating],
            name: 'kungsbacka',
            addresses: ['sweden,kungsbacka,kabelgatan 1', 'sweden,kungsbacka,kungsgatan 42'],
          },
        },
        meters: {
          1: {
            address: 'kabelgatan 1',
            city: 'sweden,kungsbacka',
            id: 1,
            medium: Medium.districtHeating,
            name: 'extId1',
          },
          2: {
            address: 'kabelgatan 1',
            city: 'sweden,kungsbacka',
            id: 2,
            medium: Medium.districtHeating,
            name: 'extId2',
          },
          3: {
            address: 'kungsgatan 42',
            city: 'sweden,gothenburg',
            id: 3,
            medium: Medium.districtHeating,
            name: 'extId3',
          },
          4: {
            address: 'kungsgatan 42',
            city: 'sweden,gothenburg',
            id: 4,
            medium: Medium.districtHeating,
            name: 'extId4',
          },
          5: {
            address: 'kungsgatan 42',
            city: 'sweden,kungsbacka',
            id: 5,
            medium: Medium.districtHeating,
            name: 'extId5',
          },
          6: {
            address: 'kungsgatan 42',
            city: 'sweden,kungsbacka',
            id: 6,
            medium: Medium.districtHeating,
            name: 'extId6',
          },
        },
      },
      result: {
        cities: [
          'sweden,kungsbacka',
          'sweden,gothenburg',
        ],
      },
    };
    expect(normalizedResponse).toEqual(expected);
  });

  it('fetches data from /selection-tree', async () => {
    await getSelectionTreeWithResponseOk();

    expect(store.getActions()).toEqual([
      actions.request(),
      actions.success(normalizedResponse),
    ]);
  });

  it('doesnt fetch data if already fetching', async () => {
    store = configureMockStore({selectionTree: {...initialSelectionTreeState, isFetching: true}});

    await getSelectionTreeWithResponseOk();

    expect(store.getActions()).toEqual([]);
  });

  it('doesnt fetch data if already successfully fetched', async () => {
    store = configureMockStore({
      selectionTree: {
        ...initialSelectionTreeState,
        isSuccessfullyFetched: true,
      },
    });

    await getSelectionTreeWithResponseOk();

    expect(store.getActions()).toEqual([]);
  });

  it('doesnt fetch data if fetched with an error', async () => {
    store = configureMockStore({
      selectionTree: {
        ...initialSelectionTreeState,
        error: {message: 'an error'},
      },
    });

    await getSelectionTreeWithResponseOk();

    expect(store.getActions()).toEqual([]);
  });

  it('dispatches a fail action when receiving an error during request', async () => {
    await getSelectionTreeWithBadRequest();

    expect(store.getActions()).toEqual([
      actions.request(),
      actions.failure({message: 'an error'}),
    ]);
  });

  describe('invalid token', () => {

    it('dispatches a logout action if token is invalid', async () => {
      const user: User = {
        id: 1,
        name: 'al',
        email: 'al@la.se',
        language: 'en',
        organisation: {id: 1, name: 'elvaco', slug: 'elvaco'},
        roles: [],
      };
      const initialState = {
        selectionTree: initialSelectionTreeState,
        auth: {
          user,
          isAuthenticated: true,
        },
      };
      store = configureMockStore(initialState);

      const error = new InvalidToken('Token missing or invalid');

      const getSelectionTreeInvalidToken = async () => {
        mockRestClient.onGet(EndPoints.selectionTree).reply(401, error);
        mockRestClient.onGet(EndPoints.logout).reply(204);
        return store.dispatch(fetchSelectionTree());
      };

      await getSelectionTreeInvalidToken();

      expect(store.getActions()).toEqual([
        actions.request(),
        logoutUser(error as Unauthorized),
        routerActions.push(`${routes.login}/${initialState.auth.user.organisation.slug}`),
      ]);
    });
  });

  describe('network error', () => {

    it('display error message when there is not internet connection', async () => {
      const getSelectionTreeWhenOffline = async () => {
        mockRestClient.onGet(EndPoints.selectionTree).networkError();
        return store.dispatch(fetchSelectionTree());
      };

      await getSelectionTreeWhenOffline();

      expect(store.getActions()).toEqual([
        actions.request(),
        actions.failure(noInternetConnection()),
      ]);
    });
  });

  describe('request timeout', () => {

    it('display error message when the request times out', async () => {
      const fetchSelectionTreeAndTimeout = async () => {
        mockRestClient.onGet(EndPoints.selectionTree).timeout();
        return store.dispatch(fetchSelectionTree());
      };

      await fetchSelectionTreeAndTimeout();

      expect(store.getActions()).toEqual([
        actions.request(),
        actions.failure(requestTimeout()),
      ]);
    });
  });

});

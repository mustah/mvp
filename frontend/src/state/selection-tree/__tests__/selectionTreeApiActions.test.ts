import {normalize} from 'normalizr';
import {routerActions} from 'react-router-redux';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {routes} from '../../../app/routes';
import {InvalidToken} from '../../../exceptions/InvalidToken';
import {initLanguage} from '../../../i18n/i18n';
import {RootState} from '../../../reducers/rootReducer';
import {EndPoints} from '../../../services/endPoints';
import {restClient, restClientWith} from '../../../services/restClient';
import {logoutUser} from '../../../usecases/auth/authActions';
import {Unauthorized} from '../../../usecases/auth/authModels';
import {User} from '../../domain-models/user/userModels';
import {makeActionsOf} from '../../summary/summaryApiActions';
import {fetchSelectionTree} from '../selectionTreeApiActions';
import {NormalizedSelectionTree} from '../selectionTreeModels';
import {initialState as initialSelectionTreeState} from '../selectionTreeReducer';
import {selectionTreeSchema} from '../selectionTreeSchemas';
import MockAdapter = require('axios-mock-adapter');

const configureMockStore = configureStore([thunk]);
let store;
restClientWith('123123123');
const mockRestClient = new MockAdapter(restClient);

describe('selectionTreeApiActions', () => {

  beforeEach(() => {
    const initialState: Partial<RootState> = {
      selectionTree: {...initialSelectionTreeState},
    };
    store = configureMockStore({...initialState});
  });
  afterEach(() => {
    mockRestClient.reset();
  });

  const responseFromApi = {
    cities: [
      {
        id: 'sweden,kungsbacka', name: 'kungsbacka', addresses: [
          {
            name: 'kabelgatan 1', meters: [
              {id: 1, name: 'extId1'},
              {id: 2, name: 'extId2'},
            ],
          },
          {
            name: 'kungsgatan 42', meters: [
              {id: 5, name: 'extId5'},
              {id: 6, name: 'extId6'},
            ],
          },
        ],
      },
      {
        id: 'sweden,gothenburg', name: 'gothenburg', addresses: [
          {
            name: 'kungsgatan 42', meters: [
              {id: 3, name: 'extId3'},
              {id: 4, name: 'extId4'},
            ],
          },
        ],
      },
    ],
  };
  const normalizedResponse = normalize(responseFromApi, selectionTreeSchema);

  const selectionTreeRequest = makeActionsOf<NormalizedSelectionTree>(EndPoints.selectionTree);
  const getSelectionTreeWithResponseOk = async () => {
    mockRestClient.onGet(EndPoints.selectionTree).reply(201, responseFromApi);
    return store.dispatch(fetchSelectionTree());
  };

  const getSelectionTreeWithBadRequest = async () => {
    mockRestClient.onGet(EndPoints.selectionTree).reply(401, {message: 'an error'});
    return store.dispatch(fetchSelectionTree());
  };

  it('normalizes data', () => {
    expect(normalizedResponse).toEqual(
      {
        entities: {
          addresses: {
            'sweden,kungsbacka,kabelgatan 1': {
              id: 'sweden,kungsbacka,kabelgatan 1',
              name: 'kabelgatan 1',
              meters: [1, 2],
            },
            'sweden,kungsbacka,kungsgatan 42': {
              id: 'sweden,kungsbacka,kungsgatan 42',
              name: 'kungsgatan 42',
              meters: [5, 6],
            },
            'sweden,gothenburg,kungsgatan 42': {
              id: 'sweden,gothenburg,kungsgatan 42',
              name: 'kungsgatan 42',
              meters: [3, 4],
            },
          },
          cities: {
            'sweden,gothenburg': {
              id: 'sweden,gothenburg',
              name: 'gothenburg',
              addresses: ['sweden,gothenburg,kungsgatan 42'],
            },
            'sweden,kungsbacka': {
              id: 'sweden,kungsbacka',
              name: 'kungsbacka',
              addresses: ['sweden,kungsbacka,kabelgatan 1', 'sweden,kungsbacka,kungsgatan 42'],
            },
          },
          meters: {
            1: {
              id: 1,
              name: 'extId1',
            },
            2: {
              id: 2,
              name: 'extId2',
            },
            3: {
              id: 3,
              name: 'extId3',
            },
            4: {
              id: 4,
              name: 'extId4',
            },
            5: {
              id: 5,
              name: 'extId5',
            },
            6: {
              id: 6,
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
      },
    );
  });

  it('fetches data from /selection-tree', async () => {
    await getSelectionTreeWithResponseOk();

    expect(store.getActions()).toEqual([
      selectionTreeRequest.request(),
      selectionTreeRequest.success(normalizedResponse),
    ]);
  });

  it('doesnt fetch data if already fetching', async () => {
    store = configureMockStore({selectionTree: {...initialSelectionTreeState, isFetching: true}});

    await getSelectionTreeWithResponseOk();

    expect(store.getActions()).toEqual([]);
  });

  it('doesnt fetch data if already successfully fetched', async () => {
    store = configureMockStore({selectionTree: {...initialSelectionTreeState, isSuccessfullyFetched: true}});

    await getSelectionTreeWithResponseOk();

    expect(store.getActions()).toEqual([]);
  });

  it('doesnt fetch data if fetched with an error', async () => {
    store = configureMockStore({selectionTree: {...initialSelectionTreeState, error: {message: 'an error'}}});

    await getSelectionTreeWithResponseOk();

    expect(store.getActions()).toEqual([]);
  });

  it('dispatches a fail action when receiving an error during request', async () => {
    await getSelectionTreeWithBadRequest();

    expect(store.getActions()).toEqual([
      selectionTreeRequest.request(),
      selectionTreeRequest.failure({message: 'an error'}),
    ]);
  });

  describe('invalid token', () => {

    it('dispatches a logout action if token is invalid', async () => {
      initLanguage({code: 'en', name: 'english'});
      const user: User = {
        id: 1,
        name: 'al',
        email: 'al@la.se',
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
        selectionTreeRequest.request(),
        logoutUser(error as Unauthorized),
        routerActions.push(`${routes.login}/${initialState.auth.user.organisation.slug}`),
      ]);
    });
  });
});

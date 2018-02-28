import axios from 'axios';
import {normalize} from 'normalizr';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {testData} from '../../../__tests__/testDataFactory';
import {initLanguage} from '../../../i18n/i18n';
import {authenticate} from '../../../services/restClient';
import {IdNamed} from '../../../types/Types';
import {authSetUser} from '../../../usecases/auth/authActions';
import {showFailMessage, showSuccessMessage} from '../../ui/message/messageActions';
import {paginationUpdateMetaData} from '../../ui/pagination/paginationActions';
import {limit} from '../../ui/pagination/paginationReducer';
import {DomainModelsState, EndPoints, HttpMethod, Normalized} from '../domainModels';
import {
  addUser, clearErrorGateways,
  deleteUser, DOMAIN_MODELS_CLEAR_ERROR, fetchGateways,
  fetchSelections,
  fetchUser,
  modifyProfile,
  modifyUser,
  requestMethod,
} from '../domainModelsActions';
import {initialDomain} from '../domainModelsReducer';
import {selectionsSchema} from '../domainModelsSchemas';
import {Gateway} from '../gateway/gatewayModels';
import {gatewaySchema} from '../gateway/gatewaySchema';
import {Organisation} from '../organisation/organisationModels';
import {addOrganisation} from '../organisation/organisationsApiActions';
import {Role, User} from '../user/userModels';
import MockAdapter = require('axios-mock-adapter');

const configureMockStore = configureStore([thunk]);

describe('domainModelsActions', () => {

  initLanguage({code: 'en', name: 'english'});
  let mockRestClient: MockAdapter;
  let store;
  const selectionsRequest = requestMethod<Normalized<IdNamed>>(EndPoints.selections, HttpMethod.GET);
  const gatewayRequest = requestMethod<Normalized<Gateway>>(EndPoints.gateways, HttpMethod.GET);
  const userPostRequest = requestMethod<User>(EndPoints.users, HttpMethod.POST);
  const organisationPostRequest = requestMethod<Organisation>(EndPoints.organisations, HttpMethod.POST);
  const userPutRequest = requestMethod<User>(EndPoints.users, HttpMethod.PUT);
  const userDeleteRequest = requestMethod<User>(EndPoints.users, HttpMethod.DELETE);
  const userEntityRequest = requestMethod<User>(EndPoints.users, HttpMethod.GET_ENTITY);

  beforeEach(() => {
    const initialState: Partial<DomainModelsState> = {
      cities: {...initialDomain()},
      gateways: {...initialDomain()},
      users: {...initialDomain()},
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
        selectionsRequest.success(normalize(testData.selections, selectionsSchema)),
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
        cities: {isFetching: true, isSuccessfullyFetched: false, total: 0, entities: {}, result: [1, 2]},
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

  describe('get gateways', () => {

    const getGatewaysWithResponseOk = async () => {
      mockRestClient.onGet(EndPoints.gateways).reply(200, testData.gateways);
      return store.dispatch(fetchGateways());
    };

    it('normalizes data and updates pagination metaData', async () => {
      await getGatewaysWithResponseOk();

      expect(store.getActions()).toEqual([
        gatewayRequest.request(),
        gatewayRequest.success(normalize(testData.gateways, gatewaySchema)),
        paginationUpdateMetaData({
          entityType: 'gateways',
          content: ['g1', 'g2', 'g3', 'g4', 'g5'],
          totalElements: 5,
          totalPages: Math.ceil(5 / limit),
        }),
      ]);
    });
  });

  describe('add new user', () => {

    const newUser: Partial<User> = {
      name: 'Alexander Laas',
      email: 'alexander.laas@elvaco.se',
      organisation: {id: 1, code: 'elvaco', name: 'elvaco'},
      roles: [Role.USER],
    };
    const returnedUser: Partial<User> = {...newUser, id: 1};
    const errorResponse = {message: 'An error'};
    const postUserWithResponseOk = async (user: Partial<User>) => {
      mockRestClient.onPost(EndPoints.users, user).reply(200, returnedUser);
      return store.dispatch(addUser(user as User));
    };
    const postUserWithBadRequest = async (user: Partial<User>) => {
      mockRestClient.onPost(EndPoints.users, user).reply(401, errorResponse);
      return store.dispatch(addUser(user as User));
    };

    it('sends a post request to backend and get a user with an id back', async () => {
      await postUserWithResponseOk(newUser);

      expect(store.getActions()).toEqual([
        userPostRequest.request(),
        userPostRequest.success(returnedUser as User),
        showSuccessMessage(`Successfully created the user ${returnedUser.name} (${returnedUser.email})`),
      ]);
    });
    it('send a post request to backend and get an error back', async () => {
      await postUserWithBadRequest(newUser);

      expect(store.getActions()).toEqual([
        userPostRequest.request(),
        userPostRequest.failure({...errorResponse}),
        showFailMessage(`Failed to create user: ${errorResponse.message}`),
      ]);
    });
  });

  describe('update user', () => {
    const updatedUser: User = {
      id: 1,
      name: 'Alexander Laas',
      email: 'alexander.laas@elvaco.se',
      organisation: {id: 1, code: 'elvaco', name: 'elvaco'},
      roles: [Role.USER, Role.ADMIN],
    };

    const errorResponse = {message: 'An error'};
    const putUserWithResponseOk = async (updatedUser: User) => {
      mockRestClient.onPut(EndPoints.users, updatedUser).reply(200, updatedUser);
      return store.dispatch(modifyUser(updatedUser));
    };
    const putUserWithBadRequest = async (updatedUser: User) => {
      mockRestClient.onPut(EndPoints.users, updatedUser).reply(401, errorResponse);
      return store.dispatch(modifyUser(updatedUser));
    };

    it('sends a put request to backend and get the user back', async () => {
      await putUserWithResponseOk(updatedUser);

      expect(store.getActions()).toEqual([
        userPutRequest.request(),
        userPutRequest.success(updatedUser),
        showSuccessMessage(`Successfully updated user ${updatedUser.name} (${updatedUser.email})`),
      ]);
    });
    it('sends a put request to backend and an error back', async () => {
      await putUserWithBadRequest(updatedUser);

      expect(store.getActions()).toEqual([
        userPutRequest.request(),
        userPutRequest.failure(errorResponse),
        showFailMessage(`Failed to update user: ${errorResponse.message}`),
      ]);
    });
  });

  describe('update profile', () => {
    const updatedUser: User = {
      id: 1,
      name: 'Alexander Laas',
      email: 'alexander.laas@elvaco.se',
      organisation: {id: 1, code: 'elvaco', name: 'elvaco'},
      roles: [Role.USER, Role.ADMIN],
    };

    const errorResponse = {message: 'An error'};
    const putUserWithResponseOk = async (updatedUser: User) => {
      mockRestClient.onPut(EndPoints.users, updatedUser).reply(200, updatedUser);
      return store.dispatch(modifyProfile(updatedUser));
    };
    const putUserWithBadRequest = async (updatedUser: User) => {
      mockRestClient.onPut(EndPoints.users, updatedUser).reply(401, errorResponse);
      return store.dispatch(modifyProfile(updatedUser));
    };

    it('sends a put request to backend and get the user back', async () => {
      await putUserWithResponseOk(updatedUser);

      expect(store.getActions()).toEqual([
        userPutRequest.request(),
        userPutRequest.success(updatedUser),
        showSuccessMessage(`Successfully updated profile`),
        authSetUser(updatedUser),
      ]);
    });
    it('sends a put request to backend and an error back', async () => {
      await putUserWithBadRequest(updatedUser);

      expect(store.getActions()).toEqual([
        userPutRequest.request(),
        userPutRequest.failure(errorResponse),
        showFailMessage(`Failed to update profile: ${errorResponse.message}`),
      ]);
    });
  });

  describe('delete user', () => {
    const deleteUserWithResponseOk = async (user: User) => {
      mockRestClient.onDelete(`${EndPoints.users}/${user.id}`).reply(200, user);
      return store.dispatch(deleteUser(user.id));
    };
    const deleteUserWithBadRequest = async (user: User) => {
      mockRestClient.onDelete(`${EndPoints.users}/${user.id}`).reply(401, errorResponse);
      return store.dispatch(deleteUser(user.id));
    };

    const user: User = {
      id: 3,
      name: 'Eva',
      organisation: {id: 1, name: 'elvaco', code: 'elvaco'},
      roles: [Role.USER],
      email: 'eva@elvaco.se',
    };
    const errorResponse = {message: 'An error'};

    it('sends a success delete request to backend and get the user back', async () => {
      await deleteUserWithResponseOk(user);

      expect(store.getActions()).toEqual([
        userDeleteRequest.request(),
        userDeleteRequest.success(user),
        showSuccessMessage(`Successfully deleted the user ${user.name} (${user.email})`),
      ]);
    });

    it('sends an unsuccessful delete request to backend and get an error back', async () => {
      await deleteUserWithBadRequest(user);

      expect(store.getActions()).toEqual([
        userDeleteRequest.request(),
        userDeleteRequest.failure(errorResponse),
        showFailMessage(`Failed to delete the user: ${errorResponse.message}`),
      ]);
    });
  });

  describe('fetch a single user', () => {
    const user: User = {
      id: 3,
      name: 'Eva',
      organisation: {id: 1, name: 'elvaco', code: 'elvaco'},
      roles: [Role.USER],
      email: 'eva@elvaco.se',
    };
    const errorResponse = {message: 'An error'};

    const getUserEntityWithResponseOk = async (user: User) => {
      mockRestClient.onGet(`${EndPoints.users}/${user.id}`).reply(200, user);
      return store.dispatch(fetchUser(user.id));
    };
    const getUserEntityWithBadRequest = async (user: User) => {
      mockRestClient.onGet(`${EndPoints.users}/${user.id}`).reply(401, errorResponse);
      return store.dispatch(fetchUser(user.id));
    };

    it('sends a successful get request to backend', async () => {
      await getUserEntityWithResponseOk(user);

      expect(store.getActions()).toEqual([
        userEntityRequest.request(),
        userEntityRequest.success(user),
      ]);
    });

    it('sends an unsuccessful request to backend', async () => {
      await getUserEntityWithBadRequest(user);

      expect(store.getActions()).toEqual([
        userEntityRequest.request(),
        userEntityRequest.failure(errorResponse),
      ]);
    });
    it('doesnt fetch if is already in cache', async () => {
      store = configureMockStore({domainModels: {users: {...initialDomain(), entities: {[user.id]: user}}}});

      await getUserEntityWithResponseOk(user);

      expect(store.getActions()).toEqual([]);
    });
  });

  describe('add new organisation', () => {

    const newOrganisation: Partial<Organisation> = {
      name: 'Hällesåkers IF',
      code: 'HIF',
    };
    const returnedOrganisation: Partial<Organisation> = {...newOrganisation, id: 1};
    const errorResponse = {message: 'An error'};

    const postOrganisationWithResponseOk = async (organisation: Partial<Organisation>) => {
      mockRestClient.onPost(EndPoints.organisations, organisation).reply(200, returnedOrganisation);
      return store.dispatch(addOrganisation(organisation as Organisation));
    };
    const postUserWithBadRequest = async (organisation: Partial<Organisation>) => {
      mockRestClient.onPost(EndPoints.organisations, organisation).reply(401, errorResponse);
      return store.dispatch(addOrganisation(organisation as Organisation));
    };

    it('sends a post request to backend and get a user with an id back', async () => {
      await postOrganisationWithResponseOk(newOrganisation);

      expect(store.getActions()).toEqual([
        organisationPostRequest.request(),
        organisationPostRequest.success(returnedOrganisation as Organisation),
        showSuccessMessage('Successfully created the organisation ' +
          `${returnedOrganisation.name} (${returnedOrganisation.code})`),
      ]);
    });
    it('send a post request to backend and get an error back', async () => {
      await postUserWithBadRequest(newOrganisation);

      expect(store.getActions()).toEqual([
        organisationPostRequest.request(),
        organisationPostRequest.failure({...errorResponse}),
        showFailMessage(`Failed to create organisation: ${errorResponse.message}`),
      ]);
    });
  });

  describe('clear error', () => {
    it('dispatches a clear error action', () => {
      store.dispatch(clearErrorGateways());

      expect(store.getActions()).toEqual([
        {type: DOMAIN_MODELS_CLEAR_ERROR(EndPoints.gateways)},
      ]);
    });
  });
});

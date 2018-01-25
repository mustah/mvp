import axios from 'axios';
import {normalize} from 'normalizr';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {testData} from '../../../__tests__/testDataFactory';
import {initLanguage} from '../../../i18n/i18n';
import {makeRestClient} from '../../../services/restClient';
import {IdNamed} from '../../../types/Types';
import {authSetUser} from '../../../usecases/auth/authActions';
import {showFailMessage, showSuccessMessage} from '../../ui/message/messageActions';
import {EndPoints, HttpMethod, Normalized} from '../domainModels';
import {
  addUser, deleteUser, fetchSelections, fetchUser, modifyProfile, modifyUser,
  requestMethod,
} from '../domainModelsActions';
import {selectionsSchema} from '../domainModelsSchemas';
import {Role, User} from '../user/userModels';
import MockAdapter = require('axios-mock-adapter');

const configureMockStore = configureStore([thunk]);

describe('domainModelsActions', () => {

  initLanguage({code: 'en', name: 'english'});
  let mockRestClient: MockAdapter;
  let store;
  const selectionsRequest = requestMethod<Normalized<IdNamed>>(EndPoints.selections, HttpMethod.GET);
  const userPostRequest = requestMethod<User>(EndPoints.users, HttpMethod.POST);
  const userPutRequest = requestMethod<User>(EndPoints.users, HttpMethod.PUT);
  const userDeleteRequest = requestMethod<User>(EndPoints.users, HttpMethod.DELETE);
  const userEntityRequest = requestMethod<User>(EndPoints.users, HttpMethod.GET_ENTITY);

  beforeEach(() => {
    store = configureMockStore({});
    mockRestClient = new MockAdapter(axios);
    makeRestClient('test');
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
  });
});

import axios from 'axios';
import {routerActions} from 'react-router-redux';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {routes} from '../../../../app/routes';
import {initTranslations} from '../../../../i18n/__tests__/i18nMock';
import {EndPoints} from '../../../../services/endPoints';
import {authenticate} from '../../../../services/restClient';
import {authSetUser} from '../../../../usecases/auth/authActions';
import {noInternetConnection, requestTimeout} from '../../../api/apiActions';
import {CHANGE_LANGUAGE} from '../../../language/languageActions';
import {showFailMessage, showSuccessMessage} from '../../../ui/message/messageActions';
import {DomainModelsState} from '../../domainModels';
import {
  deleteRequestOf,
  getEntityRequestOf,
  postRequestOf,
  putRequestOf,
} from '../../domainModelsActions';
import {initialDomain} from '../../domainModelsReducer';
import {addUser, deleteUser, fetchUser, modifyProfile, modifyUser} from '../userApiActions';
import {Role, User} from '../userModels';
import {default as MockAdapter} from 'axios-mock-adapter';

describe('userApiActions', () => {

  const configureMockStore = configureStore([thunk]);
  const translationsEN = {
    code: 'en',
    translation: {
      'successfully updated profile': 'successfully updated profile',
    },
  };
  const translationsSW = {
    code: 'sv',
    translation: {
      'successfully updated profile': 'updaterade profilen',
    },
  };

  initTranslations(translationsEN);

  const createUser = postRequestOf<User>(EndPoints.users);
  const updateUser = putRequestOf<User>(EndPoints.users);
  const removeUser = deleteRequestOf<User>(EndPoints.users);
  const fetchUserEntity = getEntityRequestOf<User>(EndPoints.users);

  let mockRestClient: MockAdapter;
  let store;

  const windowReload = window.location.reload;

  beforeEach(() => {
    const initialState: Partial<DomainModelsState> = {
      users: {...initialDomain()},
    };
    store = configureMockStore({
      domainModels: initialState,
      language: {language: 'en'},
    });
    mockRestClient = new MockAdapter(axios);
    window.location.reload = () => void(0);
    authenticate('test');
  });

  afterEach(() => {
    mockRestClient.reset();
    window.location.reload = windowReload;
  });

  describe('add new user', () => {
    const newUser: Partial<User> = {
      name: 'Alexander Laas',
      email: 'alexander.laas@elvaco.se',
      organisation: {id: 1, slug: 'elvaco', name: 'elvaco'},
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
        createUser.request(),
        createUser.success(returnedUser as User),
        showSuccessMessage(`Successfully created the user ${returnedUser.name} (${returnedUser.email})`),
        routerActions.push(`${routes.adminUsers}`)
      ]);
    });

    it('send a post request to backend and get an error back', async () => {
      await postUserWithBadRequest(newUser);

      expect(store.getActions()).toEqual([
        createUser.request(),
        createUser.failure({...errorResponse}),
        showFailMessage(`Failed to create user: ${errorResponse.message}`)
      ]);
    });
  });

  describe('update user', () => {
    const updatedUser: User = {
      id: 1,
      name: 'Alexander Laas',
      email: 'alexander.laas@elvaco.se',
      language: 'sv',
      organisation: {id: 1, slug: 'elvaco', name: 'elvaco'},
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
        updateUser.request(),
        updateUser.success(updatedUser),
        showSuccessMessage(`Successfully updated user ${updatedUser.name} (${updatedUser.email})`),
      ]);
    });

    it('sends a put request to backend and an error back', async () => {
      await putUserWithBadRequest(updatedUser);

      expect(store.getActions()).toEqual([
        updateUser.request(),
        updateUser.failure(errorResponse),
        showFailMessage(`Failed to update user: ${errorResponse.message}`),
      ]);
    });
  });

  describe('update profile', () => {
    const updatedUser: User = {
      id: 1,
      name: 'Alexander Laas',
      email: 'alexander.laas@elvaco.se',
      language: 'sv',
      organisation: {id: 1, slug: 'elvaco', name: 'elvaco'},
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
      initTranslations(translationsSW);
      await putUserWithResponseOk(updatedUser);

      expect(store.getActions()).toEqual([
        updateUser.request(),
        updateUser.success(updatedUser),
        authSetUser(updatedUser),
        {type: CHANGE_LANGUAGE, payload: 'sv'},
        showSuccessMessage('Updaterade profilen'),
      ]);
    });

    it('sends a put request to backend and an error back', async () => {
      await putUserWithBadRequest(updatedUser);

      expect(store.getActions()).toEqual([
        updateUser.request(),
        updateUser.failure(errorResponse),
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
      organisation: {id: 1, name: 'elvaco', slug: 'elvaco'},
      language: 'sv',
      roles: [Role.USER],
      email: 'eva@elvaco.se',
    };
    const errorResponse = {message: 'An error'};

    it('sends a success delete request to backend and get the user back', async () => {
      await deleteUserWithResponseOk(user);

      expect(store.getActions()).toEqual([
        removeUser.request(),
        removeUser.success(user),
        showSuccessMessage(`Successfully deleted the user ${user.name} (${user.email})`),
      ]);
    });

    it('sends an unsuccessful delete request to backend and get an error back', async () => {
      await deleteUserWithBadRequest(user);

      expect(store.getActions()).toEqual([
        removeUser.request(),
        removeUser.failure(errorResponse),
        showFailMessage(`Failed to delete the user: ${errorResponse.message}`),
      ]);
    });
  });

  describe('fetch a single user', () => {
    const user: User = {
      id: 3,
      name: 'Eva',
      organisation: {id: 1, name: 'elvaco', slug: 'elvaco'},
      language: 'sv',
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
        fetchUserEntity.request(),
        fetchUserEntity.success(user),
      ]);
    });

    it('sends an unsuccessful request to backend', async () => {
      await getUserEntityWithBadRequest(user);

      expect(store.getActions()).toEqual([
        fetchUserEntity.request(),
        fetchUserEntity.failure(errorResponse),
      ]);
    });

    it('doesnt fetch if is already in cache', async () => {
      store = configureMockStore({
        domainModels: {
          users: {
            ...initialDomain(),
            entities: {[user.id]: user},
          },
        },
      });

      await getUserEntityWithResponseOk(user);

      expect(store.getActions()).toEqual([]);
    });
  });

  describe('network error', () => {

    it('display error message when there is not internet connection', async () => {
      const fetchUserWhenOffline = async () => {
        mockRestClient.onGet(`${EndPoints.users}/1`).networkError();
        return store.dispatch(fetchUser(1));
      };

      await fetchUserWhenOffline();

      expect(store.getActions()).toEqual([
        fetchUserEntity.request(),
        fetchUserEntity.failure(noInternetConnection()),
      ]);
    });
  });

  describe('request timeout', () => {

    it('display error message when the request times out', async () => {
      const fetchUserAndTimeout = async () => {
        mockRestClient.onGet(`${EndPoints.users}/1`).timeout();
        return store.dispatch(fetchUser(1));
      };

      await fetchUserAndTimeout();

      expect(store.getActions()).toEqual([
        fetchUserEntity.request(),
        fetchUserEntity.failure(requestTimeout()),
      ]);
    });
  });
});

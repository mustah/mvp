import axios from 'axios';
import {normalize} from 'normalizr';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {testData} from '../../../__tests__/testDataFactory';
import {initLanguage} from '../../../i18n/i18n';
import {makeRestClient} from '../../../services/restClient';
import {IdNamed} from '../../../types/Types';
import {showMessage} from '../../ui/message/messageActions';
import {EndPoints, Normalized} from '../domainModels';
import {addUser, deleteUser, fetchSelections, HttpMethod, requestMethod} from '../domainModelsActions';
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
  const userDeleteRequest = requestMethod<User>(EndPoints.users, HttpMethod.DELETE);

  beforeEach(() => {
    store = configureMockStore({});
    mockRestClient = new MockAdapter(axios);
    makeRestClient('test');
  });

  afterEach(() => {
    mockRestClient.reset();
  });

  describe('fetch domainModels from /selections', () => {

    const fetchFakeDomainModels = async () => {
      mockRestClient.onGet(EndPoints.selections).reply(200, testData.selections);

      return store.dispatch(fetchSelections());
    };

    it('normalizes data', async () => {
      await fetchFakeDomainModels();

      expect(store.getActions()).toEqual([
        selectionsRequest.request(),
        selectionsRequest.success(normalize(testData.selections, selectionsSchema)),
      ]);
    });

    it('throws exception when no data exists', async () => {
      const response = {message: 'failed'};

      mockRestClient.onGet(EndPoints.selections).reply(401, response);

      await store.dispatch(fetchSelections());

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
    const postFakeUser = async (user: Partial<User>, success: boolean) => {
      if (success) {
        mockRestClient.onPost(EndPoints.users, user).reply(200, returnedUser);
      } else {
        mockRestClient.onPost(EndPoints.users, user).reply(401, errorResponse);
      }
      return store.dispatch(addUser(user as User));
    };

    it('sends a post request to backend and get a user with an id back', async () => {
      await postFakeUser(newUser, true);

      expect(store.getActions()).toEqual([
        userPostRequest.request(),
        userPostRequest.success(returnedUser as User),
        showMessage(`Successfully created the user ${returnedUser.name} (${returnedUser.email})`),
      ]);
    });
    it('send a post request to backend and get an error back', async () => {
      await postFakeUser(newUser, false);

      expect(store.getActions()).toEqual([
        userPostRequest.request(),
        userPostRequest.failure({...errorResponse}),
        showMessage(`Failed to create user: ${errorResponse.message}`),
      ]);
    });
  });

  describe('delete user', () => {

    const fakeDeleteUser = async (user: User, success: boolean) => {
      if (success) {
        mockRestClient.onDelete(`${EndPoints.users}/${user.id}`).reply(200, user);
      } else {
        mockRestClient.onDelete(`${EndPoints.users}/${user.id}`).reply(401, errorResponse);
      }
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

    it('sends a delete request to backend and get the user back', async () => {
      await fakeDeleteUser(user, true);

      expect(store.getActions()).toEqual([
        userDeleteRequest.request(),
        userDeleteRequest.success(user),
        showMessage(`Successfully deleted the user ${user.name} (${user.email})`),
      ]);
    });

    it('sends a delete request to backend and get an error back', async () => {
      await fakeDeleteUser(user, false);

      expect(store.getActions()).toEqual([
        userDeleteRequest.request(),
        userDeleteRequest.failure(errorResponse),
        showMessage(`Failed to delete the user: ${errorResponse.message}`),
      ]);
    });
  });
});

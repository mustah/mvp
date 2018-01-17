import {normalize} from 'normalizr';
import {testData} from '../../../__tests__/testDataFactory';
import {IdNamed} from '../../../types/Types';
import {EndPoints, HttpMethod, Normalized, SelectionEntity} from '../domainModels';
import {requestMethod} from '../domainModelsActions';
import {addresses, cities, initialDomain, users} from '../domainModelsReducer';
import {selectionsSchema} from '../domainModelsSchemas';
import {Role, User, UserState} from '../user/userModels';

describe('domainModelsReducer', () => {

  const selectionsRequest = requestMethod<Normalized<IdNamed>>(EndPoints.selections, HttpMethod.GET);

  describe('addresses', () => {

    const initialState = initialDomain<SelectionEntity>();
    it('has initial state', () => {
      expect(addresses(initialState, {type: 'unknown'})).toEqual({...initialState});
    });

    it('fetches selections for addresses', () => {
      expect(addresses(initialState, selectionsRequest.request())).toEqual({...initialState, isFetching: true});
    });

    it('has fetched selections successfully ', () => {
      const payload = normalize(testData.selections, selectionsSchema);

      expect(addresses(initialState, selectionsRequest.success(payload))).toEqual({
        ...initialState,
        entities: {
          1: {id: 1, name: 'Stampgatan 46', cityId: 'got'},
          2: {id: 2, name: 'Stampgatan 33', cityId: 'got'},
          3: {id: 3, name: 'Kungsgatan 44', cityId: 'sto'},
          4: {id: 4, name: 'Drottninggatan 1', cityId: 'mmx'},
          5: {id: 5, name: 'Åvägen 9', cityId: 'kub'},
        },
        result: [1, 2, 3, 4, 5],
        total: 5,
      });
    });

    it('has error when fetching has failed', () => {
      const payload = {message: 'failed'};

      expect(addresses(initialState, selectionsRequest.failure(payload))).toEqual({
        ...initialState,
        error: payload,
      });
    });
  });

  describe('cities', () => {

    const initialState = initialDomain<SelectionEntity>();
    it('has initial state', () => {
      expect(cities(initialState, {type: 'unknown'})).toEqual({...initialState});
    });

    it('fetches selections for cities', () => {
      expect(cities(initialState, selectionsRequest.request())).toEqual({...initialState, isFetching: true});
    });

    it('has fetched selections successfully', () => {
      const payload = normalize(testData.selections, selectionsSchema);

      expect(cities(initialState, selectionsRequest.success(payload))).toEqual({
        ...initialState,
        entities: {
          got: {id: 'got', name: 'Göteborg'},
          sto: {id: 'sto', name: 'Stockholm'},
          mmx: {id: 'mmx', name: 'Malmö'},
          kub: {id: 'kub', name: 'Kungsbacka'},
        },
        result: ['got', 'sto', 'mmx', 'kub'],
        total: 4,
      });
    });

    it('has error when fetching has failed', () => {
      const payload = {message: 'failed'};

      expect(cities(initialState, selectionsRequest.failure(payload))).toEqual({
        ...initialState,
        error: payload,
      });
    });
  });

  describe('users', () => {

    const initialState: UserState = initialDomain<User>();

    const usersPostRequest = requestMethod<User>(EndPoints.users, HttpMethod.POST);
    const usersPutRequest = requestMethod<User>(EndPoints.users, HttpMethod.PUT);
    const usersDeleteRequest = requestMethod<User>(EndPoints.users, HttpMethod.DELETE);
    const user: User = {
      id: 3,
      name: 'Eva',
      organisation: {id: 1, name: 'elvaco', code: 'elvaco'},
      roles: [Role.USER],
      email: 'eva@elvaco.se',
    };

    const populatedState: UserState = {
      result: [3],
      entities: {3: user},
      isFetching: false,
      total: 1,
    };

    it('has initial state', () => {
      expect(users(initialState, {type: 'unknown'})).toEqual({...initialState});
    });

    it('requests users', () => {
      expect(users(initialState, usersPostRequest.request())).toEqual({...initialState, isFetching: true});
      expect(users(initialState, usersDeleteRequest.request())).toEqual({...initialState, isFetching: true});
    });

    it('adds new user to state', () => {
      expect(users(initialState, usersPostRequest.success(user))).toEqual({
        result: [3],
        entities: {3: user},
        isFetching: false,
        total: 1,
      });
    });

    it('modifies a current user in the state', () => {
      const newName = 'Eva Nilsson';

      expect(users(populatedState, usersPutRequest.success({...user, name: newName}))).toEqual({
        ...populatedState,
        entities: {3: {...user, name: newName}},
      });
    });

    it('deletes a user from state', () => {
      expect(users(populatedState, usersDeleteRequest.success(user))).toEqual(initialState);
    });

    it('has error when fetching has failed', () => {
      const payload = {message: 'failed'};

      expect(users(initialState, usersPostRequest.failure(payload))).toEqual({
        ...initialState,
        error: payload,
      });
      expect(users(initialState, usersDeleteRequest.failure(payload))).toEqual({
        ...initialState,
        error: payload,
      });
    });
  });
});

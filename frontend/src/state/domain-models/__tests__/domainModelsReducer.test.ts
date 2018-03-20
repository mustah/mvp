import {normalize} from 'normalizr';
import {testData} from '../../../__tests__/testDataFactory';
import {EndPoints} from '../../../services/endPoints';
import {IdNamed} from '../../../types/Types';
import {SET_SELECTION} from '../../search/selection/selectionActions';
import {ParameterName} from '../../search/selection/selectionModels';
import {DomainModelsState, Normalized, NormalizedState, SelectionEntity} from '../domainModels';
import {deleteRequestOf, getEntityRequestOf, getRequestOf, postRequestOf, putRequestOf} from '../domainModelsActions';
import {addresses, cities, domainModels, gateways, initialDomain, users} from '../domainModelsReducer';
import {selectionsSchema} from '../selections/selectionsSchemas';
import {clearErrorGateways} from '../gateway/gatewayApiActions';
import {Gateway} from '../gateway/gatewayModels';
import {Role, User, UserState} from '../user/userModels';

describe('domainModelsReducer', () => {

  const selectionsRequest = getRequestOf<Normalized<IdNamed>>(EndPoints.selections);
  const normalizedSelections = normalize(testData.selections, selectionsSchema);

  describe('/selections', () => {
    it('does not clear on change in selections', () => {
      const emptyState: Partial<DomainModelsState> = {};
      const initialState = domainModels(emptyState as DomainModelsState, {type: 'undefined'});
      const populatedCitiesState: Partial<DomainModelsState> = {
        ...initialState,
        cities: {
          isFetching: false,
          isSuccessfullyFetched: true,
          total: 1,
          result: [1],
          entities: {1: {id: 1, name: '1'}},
        },
      };

      expect(domainModels(populatedCitiesState as DomainModelsState, {
        type: SET_SELECTION, payload: {
          id: 1,
          parameter: ParameterName.cities,
        },
      })).toEqual({
        ...populatedCitiesState,
      });
    });
  });

  describe('addresses', () => {

    const initialState = initialDomain<SelectionEntity>();
    it('has initial state', () => {
      expect(addresses(initialState, {type: 'unknown'})).toEqual({...initialState});
    });

    it('fetches selections for addresses', () => {
      expect(addresses(initialState, selectionsRequest.request())).toEqual({
        ...initialState,
        isFetching: true,
      });
    });

    it('has fetched selections successfully ', () => {

      expect(addresses(initialState, selectionsRequest.success(normalizedSelections))).toEqual({
        ...initialState,
        isSuccessfullyFetched: true,
        entities: {
          'sweden,göteborg,kungsgatan': {
            id: 'sweden,göteborg,kungsgatan',
            name: 'kungsgatan',
            parentId: 'sweden,göteborg',
          },
          'sweden,stockholm,drottninggatan': {
            id: 'sweden,stockholm,drottninggatan',
            name: 'drottninggatan',
            parentId: 'sweden,stockholm',
          },
          'sweden,stockholm,kungsgatan': {
            id: 'sweden,stockholm,kungsgatan',
            name: 'kungsgatan',
            parentId: 'sweden,stockholm',
          },
          'finland,vasa,kungsgatan': {id: 'finland,vasa,kungsgatan', name: 'kungsgatan', parentId: 'finland,vasa'},
        },
        result: [
          'sweden,göteborg,kungsgatan',
          'sweden,stockholm,kungsgatan',
          'sweden,stockholm,drottninggatan',
          'finland,vasa,kungsgatan',
        ],
        total: 4,
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
      expect(cities(initialState, selectionsRequest.request())).toEqual({
        ...initialState,
        isFetching: true,
      });
    });

    it('has fetched selections successfully', () => {

      expect(cities(initialState, selectionsRequest.success(normalizedSelections))).toEqual({
        ...initialState,
        isSuccessfullyFetched: true,
        entities: {
          'sweden,göteborg': {
            id: 'sweden,göteborg',
            name: 'göteborg',
            parentId: 'sweden',
            addresses: ['sweden,göteborg,kungsgatan'],
          },
          'sweden,stockholm': {
            id: 'sweden,stockholm',
            name: 'stockholm',
            parentId: 'sweden',
            addresses: ['sweden,stockholm,kungsgatan', 'sweden,stockholm,drottninggatan'],
          },
          'finland,vasa': {
            id: 'finland,vasa',
            name: 'vasa',
            parentId: 'finland',
            addresses: ['finland,vasa,kungsgatan'],
          },

        },
        result: ['sweden,göteborg', 'sweden,stockholm', 'finland,vasa'],
        total: 3,
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

    const createUser = postRequestOf<User>(EndPoints.users);
    const updateUser = putRequestOf<User>(EndPoints.users);
    const deleteUser = deleteRequestOf<User>(EndPoints.users);
    const usersGetUserEntity = getEntityRequestOf<User>(EndPoints.users);
    const user: User = {
      id: 3,
      name: 'Eva',
      organisation: {id: 1, name: 'elvaco', slug: 'elvaco'},
      roles: [Role.USER],
      email: 'eva@elvaco.se',
    };

    const populatedState: UserState = {
      ...initialState,
      result: [3],
      entities: {3: user},
      total: 1,
      isSuccessfullyFetched: true,
    };

    it('has initial state', () => {
      expect(users(initialState, {type: 'unknown'})).toEqual({...initialState});
    });

    it('requests users', () => {
      expect(users(initialState, createUser.request())).toEqual({
        ...initialState,
        isFetching: true,
      });
      expect(users(initialState, deleteUser.request())).toEqual({
        ...initialState,
        isFetching: true,
      });
    });

    it('adds new user to state', () => {
      expect(users(initialState, createUser.success(user))).toEqual({
        result: [3],
        entities: {3: user},
        isSuccessfullyFetched: false,
        isFetching: false,
        total: 1,
      });
    });

    it('modifies a current user in the state', () => {
      const newName = 'Eva Nilsson';

      expect(users(populatedState, updateUser.success({...user, name: newName}))).toEqual({
        ...populatedState,
        entities: {3: {...user, name: newName}},
      });
    });

    it('deletes a user from state', () => {
      expect(users(populatedState, deleteUser.success(user))).toEqual({
        ...initialState,
        isSuccessfullyFetched: true,
      });
    });

    it('has error when fetching has failed', () => {
      const payload = {message: 'failed'};

      expect(users(initialState, createUser.failure(payload))).toEqual({
        ...initialState,
        error: payload,
      });
    });

    it('adds a user to state', () => {
      expect(users(initialState, usersGetUserEntity.success(user))).toEqual({
        result: [3],
        entities: {3: user},
        isSuccessfullyFetched: false,
        isFetching: false,
        total: 1,
      });
    });
  });

  describe('clear domainModels', () => {
    it('resets all domain models', () => {
      const errorState: NormalizedState<Gateway> = {
        ...initialDomain<Gateway>(),
        isSuccessfullyFetched: true,
        error: {message: 'an error'},
      };

      expect(gateways(errorState, clearErrorGateways())).toEqual({
        ...initialDomain<Gateway>(),
      });
    });
  });

  describe('clear domainModels', () => {
    it('resets all domain models', () => {
      const initialState: DomainModelsState = {
        countries: initialDomain(),
        cities: initialDomain(),
        addresses: initialDomain(),
        gateways: initialDomain(),
        alarms: initialDomain(),
        gatewayStatuses: initialDomain(),
        measurements: initialDomain(),
        allMeters: initialDomain(),
        meterStatuses: initialDomain(),
        users: initialDomain(),
        organisations: initialDomain(),
      };
      const nonInitialState: DomainModelsState = {
        ...initialState,
        gateways: {...initialState.gateways, isFetching: true},
      };

      expect(domainModels(nonInitialState, {type: SET_SELECTION, payload: 'irrelevant'})).toEqual({
        ...initialState,
      });
    });
  });

});

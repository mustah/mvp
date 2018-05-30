import {mockSelectionAction} from '../../../__tests__/testActions';
import {testData} from '../../../__tests__/testDataFactory';
import {EndPoints} from '../../../services/endPoints';
import {Action, IdNamed} from '../../../types/Types';
import {LOGOUT_USER} from '../../../usecases/auth/authActions';
import {clearErrorGatewayMapMarkers} from '../../../usecases/map/mapMarkerActions';
import {MapMarker} from '../../../usecases/map/mapModels';
import {Gateway} from '../../domain-models-paginated/gateway/gatewayModels';
import {ADD_PARAMETER_TO_SELECTION} from '../../user-selection/userSelectionActions';
import {ParameterName} from '../../user-selection/userSelectionModels';
import {DomainModelsState, Normalized, NormalizedState, SelectionEntity} from '../domainModels';
import {
  deleteRequestOf,
  domainModelsGetEntitySuccess,
  domainModelsGetSuccess,
  getEntityRequestOf,
  getRequestOf,
  postRequestOf,
  putRequestOf,
} from '../domainModelsActions';
import {
  addresses,
  cities,
  domainModels,
  gatewayMapMarkers,
  initialDomain,
  meterMapMarkers,
  users,
} from '../domainModelsReducer';
import {selectionsDataFormatter} from '../selections/selectionsSchemas';
import {Role, User, UserState} from '../user/userModels';

describe('domainModelsReducer', () => {

  const selectionsRequest = getRequestOf<Normalized<IdNamed>>(EndPoints.selections);
  const normalizedSelections = selectionsDataFormatter(testData.selections);

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
        type: ADD_PARAMETER_TO_SELECTION, payload: {
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
          'finland,vasa,kungsgatan': {
            id: 'finland,vasa,kungsgatan',
            name: 'kungsgatan',
            parentId: 'finland,vasa',
          },
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
      language: 'en',
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

  describe('gatewayMapMarkers', () => {

    it('stores empty result as object, not undefined', () => {
      const emptyState: NormalizedState<MapMarker> = {
        ...initialDomain<MapMarker>(),
        isSuccessfullyFetched: true,
      };

      const gatewayAction: Action<Normalized<MapMarker>> = {
        type: domainModelsGetSuccess(EndPoints.gatewayMapMarkers),
        payload: {
          result: [],
          entities: {},
        },
      };

      expect(gatewayMapMarkers(emptyState, gatewayAction)).toEqual({
        ...initialDomain<Gateway>(),
        isSuccessfullyFetched: true,
      });
    });
  });

  describe('meterMapMarkers', () => {

    it('stores empty result as object, not undefined', () => {
      const emptyState: NormalizedState<MapMarker> = {
        ...initialDomain<MapMarker>(),
      };

      const meterAction: Action<Normalized<MapMarker>> = {
        type: domainModelsGetSuccess(EndPoints.meterMapMarkers),
        payload: {
          result: [],
          entities: {},
        },
      };

      expect(meterMapMarkers(emptyState, meterAction)).toEqual({
        ...initialDomain<Gateway>(),
        isSuccessfullyFetched: true,
      });
    });

    it('handles null payload', () => {
      const emptyState: NormalizedState<MapMarker> = {
        ...initialDomain<MapMarker>(),
      };

      const meterMapMarkerAction: Action<Partial<MapMarker>> = {
        type: domainModelsGetEntitySuccess(EndPoints.meterMapMarkers),
        payload: {
          id: 1,
        },
      };

      expect(meterMapMarkers(emptyState, meterMapMarkerAction)).toEqual({
        result: [1],
        entities: {1: {id: 1}},
        isFetching: false,
        isSuccessfullyFetched: false,
        total: 1,
      });
    });
  });

  describe('clear domainModels', () => {
    it('resets all domain models', () => {
      const errorState: NormalizedState<MapMarker> = {
        ...initialDomain<MapMarker>(),
        isSuccessfullyFetched: true,
        error: {message: 'an error'},
      };

      expect(gatewayMapMarkers(errorState, clearErrorGatewayMapMarkers())).toEqual({
        ...initialDomain<Gateway>(),
      });
    });
  });

  describe('logout user', () => {

    it('resets state to initial state', () => {
      let state: NormalizedState<MapMarker> = {
        ...initialDomain<MapMarker>(),
        isSuccessfullyFetched: true,
        error: {message: 'an error'},
      };

      state = gatewayMapMarkers(state, {type: LOGOUT_USER});

      const expected: NormalizedState<MapMarker> = {...initialDomain<MapMarker>()};

      expect(state).toEqual(expected);
    });
  });

  describe('clear domainModels', () => {
    it('resets all domain models except the ones related to selection drop downs', () => {
      const initialState: DomainModelsState = {
        addresses: initialDomain(),
        alarms: initialDomain(),
        cities: initialDomain(),
        countries: initialDomain(),
        gatewayMapMarkers: initialDomain(),
        gatewayStatuses: initialDomain(),
        media: initialDomain(),
        meters: initialDomain(),
        meterMapMarkers: initialDomain(),
        meterStatuses: initialDomain(),
        organisations: initialDomain(),
        userSelections: initialDomain(),
        users: initialDomain(),
      };
      const isFetchingState: DomainModelsState = {
        addresses: {...initialState.addresses, isFetching: true},
        alarms: {...initialState.alarms, isFetching: true},
        cities: {...initialState.cities, isFetching: true},
        countries: {...initialState.countries, isFetching: true},
        gatewayMapMarkers: {...initialState.gatewayMapMarkers, isFetching: true},
        gatewayStatuses: {...initialState.gatewayStatuses, isFetching: true},
        media: {...initialState.media, isFetching: true},
        meters: {...initialState.meters, isFetching: true},
        meterMapMarkers: {...initialState.meterMapMarkers, isFetching: true},
        meterStatuses: {...initialState.meterStatuses, isFetching: true},
        organisations: {...initialState.organisations, isFetching: true},
        userSelections: initialDomain(),
        users: {...initialState.users, isFetching: true},
      };

      const expected: DomainModelsState = {
        ...initialState,
        countries: {...isFetchingState.countries},
        cities: {...isFetchingState.cities},
        addresses: {...isFetchingState.addresses},
        alarms: {...isFetchingState.alarms},
        gatewayStatuses: {...isFetchingState.gatewayStatuses},
        media: {...isFetchingState.media},
        meterStatuses: {...isFetchingState.meterStatuses},
      };
      expect(domainModels(isFetchingState, mockSelectionAction)).toEqual(expected);
    });
  });

});

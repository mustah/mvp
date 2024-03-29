import {Period} from '../../../components/dates/dateModels';
import {getId, groupById} from '../../../helpers/collections';
import {EndPoints} from '../../../services/endPoints';
import {Action, Status} from '../../../types/Types';
import {logoutUser} from '../../../usecases/auth/authActions';
import {setMeterCollectionStatsTimePeriod} from '../../../usecases/collection/collectionActions';
import {clearErrorGatewayMapMarkers} from '../../../usecases/map/mapMarkerActions';
import {MapMarker} from '../../../usecases/map/mapModels';
import {meterDetailMeasurementRequest} from '../../../usecases/meter/measurements/meterDetailMeasurementActions';
import {Gateway} from '../../domain-models-paginated/gateway/gatewayModels';
import {search} from '../../search/searchActions';
import {makeMeterQuery} from '../../search/searchModels';
import {initialState as initialMeasurementState} from '../../ui/graph/measurement/measurementReducer';
import {unknownAction} from '../../ui/tabs/tabsActions';
import {resetSelection} from '../../user-selection/userSelectionActions';
import {SelectionInterval} from '../../user-selection/userSelectionModels';
import {CollectionStat} from '../collection-stat/collectionStatModels';
import {DomainModelsState, Normalized, NormalizedState} from '../domainModels';
import {
  deleteRequestOf,
  domainModelsGetSuccess,
  getEntityRequestOf,
  getRequestOf,
  postRequestOf,
  putRequestOf,
} from '../domainModelsActions';
import {
  domainModels,
  gatewayMapMarkers,
  initialDomain,
  meterCollectionStats,
  meterMapMarkers,
  organisations,
  users
} from '../domainModelsReducer';
import {Organisation} from '../organisation/organisationModels';
import {Role, User, UserState} from '../user/userModels';

describe('domainModelsReducer', () => {

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
      roles: [Role.MVP_USER],
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
      expect(users(initialState, unknownAction())).toEqual({...initialState});
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

  describe('organisations', () => {

    const initialState: NormalizedState<Organisation> = initialDomain<Organisation>();

    const respondSuccessWith = (orgs: Organisation[]) =>
      getRequestOf<Normalized<Organisation>>(EndPoints.organisations)
        .success({
          result: orgs.map(getId),
          entities: {
            organisations: groupById(orgs)
          },
        });

    const organisationWithoutParent: Organisation = {
      name: 'org 1',
      id: 'org 1',
      slug: 'org-1',
    };

    const organisationWithParent: Organisation = {
      ...organisationWithoutParent,
      parent: {
        name: 'org 0',
        id: 'org 0',
        slug: 'org-0',
      }
    };

    it('accepts organisation without parent', async () => {
      const organisation: Organisation = {...organisationWithoutParent};

      const action: Action<Normalized<Organisation>> = respondSuccessWith([organisation]);
      const state: NormalizedState<Organisation> = organisations(initialState, action);

      expect(state).toHaveProperty('entities', {[organisation.id]: {...organisation}});
    });

    it('accepts organisation with parent', async () => {
      const organisation: Organisation = {...organisationWithParent};

      const action: Action<Normalized<Organisation>> = respondSuccessWith([organisation]);
      const state: NormalizedState<Organisation> = organisations(initialState, action);

      expect(state).toHaveProperty('entities', {[organisation.id]: {...organisation}});
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
      const emptyState: NormalizedState<MapMarker> = initialDomain<MapMarker>();

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

    it('reset meterMapMarkers state when new search query is typed', () => {
      const initialState: NormalizedState<MapMarker> = initialDomain<MapMarker>();

      const meterAction: Action<Normalized<MapMarker>> = {
        type: domainModelsGetSuccess(EndPoints.meterMapMarkers),
        payload: {
          result: [1],
          entities: {
            meterMapMarkers: {
              1: {id: 1, status: Status.ok, latitude: 1.2, longitude: 2.2},
            },
          },
        },
      };

      const nextState = meterMapMarkers(initialState, meterAction);

      const expected: NormalizedState<MapMarker> = {
        result: [1],
        entities: {1: {id: 1, status: Status.ok, latitude: 1.2, longitude: 2.2}},
        isFetching: false,
        isSuccessfullyFetched: true,
        total: 1,
      };

      expect(nextState).toEqual(expected);

      const state = meterMapMarkers(nextState, search(makeMeterQuery('test')));

      expect(state).toEqual(initialDomain<MapMarker>());
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
      const initialState: NormalizedState<MapMarker> = {
        ...initialDomain<MapMarker>(),
        isSuccessfullyFetched: true,
        error: {message: 'an error'},
      };

      const state = gatewayMapMarkers(initialState, logoutUser(undefined));

      const expected: NormalizedState<MapMarker> = {...initialDomain<MapMarker>()};

      expect(state).toEqual(expected);
    });
  });

  describe('clear domainModels', () => {

    const initialState: DomainModelsState = {
      allCollectionStats: initialDomain(),
      gatewayMapMarkers: initialDomain(),
      legendItems: initialDomain(),
      meters: initialDomain(),
      meterMapMarkers: initialDomain(),
      organisations: initialDomain(),
      userSelections: initialDomain(),
      users: initialDomain(),
      mediums: initialDomain(),
      meterDefinitions: initialDomain(),
      quantities: initialDomain(),
      collectionStats: initialDomain(),
      meterCollectionStats: initialDomain(),
      meterDetailMeasurement: initialMeasurementState,
      dashboards: initialDomain(),
      widgets: initialDomain(),
      subOrganisations: initialDomain(),
    };

    const state: DomainModelsState = {
      allCollectionStats: {...initialState.allCollectionStats, isFetching: true},
      gatewayMapMarkers: {...initialState.gatewayMapMarkers, isFetching: true},
      legendItems: {...initialState.legendItems, isFetching: true},
      meters: {...initialState.meters, isFetching: true},
      meterMapMarkers: {...initialState.meterMapMarkers, isFetching: true},
      organisations: {...initialState.organisations, isFetching: true},
      userSelections: {...initialState.userSelections, isFetching: false},
      users: {...initialState.users, isFetching: true},
      mediums: {...initialState.mediums, isFetching: true},
      meterDefinitions: {...initialState.meterDefinitions, isFetching: true},
      quantities: {...initialState.quantities, isFetching: true},
      collectionStats: {...initialState.collectionStats, isFetching: true},
      meterCollectionStats: {...initialState.collectionStats, isFetching: true},
      meterDetailMeasurement: {...initialMeasurementState, isFetching: true},
      dashboards: {...initialState.dashboards, isFetching: false},
      widgets: {...initialState.widgets, isFetching: false},
      subOrganisations: {...initialState.subOrganisations, isFetching: true},
    };

    it('resets all domain models except the ones related to selection drop downs and dashboard', () => {
      expect(domainModels(state, resetSelection())).toEqual(initialState);
    });
  });

  describe('collection stats reducer', () => {
    const state: NormalizedState<CollectionStat> = {...initialDomain(), isFetching: true, total: 100};

    it('will reset to initial state when period is changed', () => {
      const payload: SelectionInterval = {period: Period.currentMonth};

      expect(meterCollectionStats(state, setMeterCollectionStatsTimePeriod(payload))).toEqual(initialDomain());
    });

    it('will reset to initial state a new meter details request has been initialized', () => {
      expect(meterCollectionStats(state, meterDetailMeasurementRequest())).toEqual(initialDomain());
    });
  });

});

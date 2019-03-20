import {LOCATION_CHANGE} from 'react-router-redux';
import {combineReducers} from 'redux';
import {getType} from 'typesafe-actions';
import {EmptyAction} from 'typesafe-actions/dist/types';
import {resetReducer} from '../../reducers/resetReducer';
import {EndPoints} from '../../services/endPoints';
import {Action, ErrorResponse, Identifiable, uuid} from '../../types/Types';
import {logoutUser} from '../../usecases/auth/authActions';
import {MapMarker} from '../../usecases/map/mapModels';
import {meterDetailMeasurement} from '../../usecases/meter/measurements/meterDetailMeasurementReducer';
import {search} from '../../usecases/search/searchActions';
import {QueryParameter} from '../../usecases/search/searchModels';
import {UserSelection} from '../user-selection/userSelectionModels';
import {CollectionStat} from './collection-stat/collectionStatModels';
import {DomainModelsState, Normalized, NormalizedState, ObjectsById} from './domainModels';
import {
  domainModelsClearError,
  domainModelsDeleteSuccess,
  domainModelsFailure,
  domainModelsGetEntitiesSuccess,
  domainModelsGetEntitySuccess,
  domainModelsGetSuccess,
  domainModelsPostSuccess,
  domainModelsPutSuccess,
  domainModelsRequest,
} from './domainModelsActions';
import {Medium, MeterDefinition, Quantity} from './meter-definitions/meterDefinitionModels';
import {MeterDetails} from './meter-details/meterDetailsModels';
import {Organisation} from './organisation/organisationModels';
import {User} from './user/userModels';

export const initialDomain = <T extends Identifiable>(): NormalizedState<T> => ({
  result: [],
  entities: {},
  isFetching: false,
  isSuccessfullyFetched: false,
  total: 0,
});

const setEntities = <T extends Identifiable>(
  entity: string,
  state: NormalizedState<T>,
  payload: Normalized<T>,
): NormalizedState<T> => {
  const entities: ObjectsById<T> = entity in payload.entities ? payload.entities[entity] : {};
  const result: uuid[] = Array.isArray(payload.result) ? payload.result : Object.keys(entities);
  return {
    ...state,
    isFetching: false,
    isSuccessfullyFetched: true,
    entities,
    result,
    total: result.length,
  };
};

const addEntities = <T extends Identifiable>(
  entity: string,
  state: NormalizedState<T>,
  payload: Normalized<T>,
): NormalizedState<T> => {
  const entities: ObjectsById<T> = entity in payload.entities ? payload.entities[entity] : {};
  const result: uuid[] = [...state.result, ...payload.result];
  return {
    ...state,
    isFetching: false,
    entities: {...state.entities, ...entities},
    result,
    total: result.length,
  };
};

const addEntity =
  <T extends Identifiable>(state: NormalizedState<T>, {payload}: Action<T>): NormalizedState<T> => {
    const result: uuid[] = [...state.result, payload.id];
    return {
      ...state,
      isFetching: false,
      entities: {...state.entities, [payload.id]: payload},
      result,
      total: result.length,
    };
  };

const modifyEntity =
  <T extends Identifiable>(
    state: NormalizedState<T>,
    {payload}: Action<T>,
  ): NormalizedState<T> => ({
    ...state,
    isFetching: false,
    entities: {...state.entities, [payload.id]: payload},
  });

const removeEntity =
  <T extends Identifiable>(
    state: NormalizedState<T>,
    {payload: {id: idToDelete}}: Action<T>,
  ): NormalizedState<T> => {
    const result: uuid[] = state.result.filter((id) => id !== idToDelete);
    const {[idToDelete]: deletedItem, ...entities}: ObjectsById<T> = state.entities;
    return {
      ...state,
      entities,
      isFetching: false,
      result,
      total: result.length,
    };
  };

const clearDomainModelErrors = <T extends Identifiable>(
  state: NormalizedState<T>
): NormalizedState<T> =>
  ({
    ...state,
    error: undefined
  });

const setError = <T extends Identifiable>(
  state: NormalizedState<T>,
  {payload: error}: Action<ErrorResponse>,
): NormalizedState<T> => ({
  ...state,
  isFetching: false,
  isSuccessfullyFetched: false,
  error,
});

type ActionTypes<T extends Identifiable> =
  | EmptyAction<string>
  | Action<Normalized<T>>
  | Action<QueryParameter>
  | Action<T>
  | Action<ErrorResponse>;

const reducerFor = <T extends Identifiable>(
  entity: keyof DomainModelsState,
  endPoint: EndPoints,
  resetState = identity,
) =>
  (
    state: NormalizedState<T> = initialDomain<T>(),
    action: ActionTypes<T>,
  ): NormalizedState<T> => {

    switch (action.type) {
      case domainModelsRequest(endPoint):
        return {...state, isFetching: true};
      case domainModelsGetSuccess(endPoint):
        return setEntities<T>(entity, state, (action as Action<Normalized<T>>).payload);
      case domainModelsGetEntitiesSuccess(endPoint):
        return addEntities<T>(entity, state, (action as Action<Normalized<T>>).payload);
      case domainModelsGetEntitySuccess(endPoint):
        return addEntity<T>(state, action as Action<T>);
      case domainModelsPostSuccess(endPoint):
        return addEntity<T>(state, action as Action<T>);
      case domainModelsPutSuccess(endPoint):
        return modifyEntity<T>(state, action as Action<T>);
      case domainModelsDeleteSuccess(endPoint):
        return removeEntity<T>(state, action as Action<T>);
      case domainModelsFailure(endPoint):
        return setError(state, action as Action<ErrorResponse>);
      case LOCATION_CHANGE:
        return clearDomainModelErrors<T>(state);
      case domainModelsClearError(endPoint):
      case getType(search):
        return initialDomain<T>();
      default:
        return resetState(state, action, endPoint);
    }
  };

const identity = (state, action, endPoint) => state;

const resetStateReducer = <T extends Identifiable>(
  state: NormalizedState<T> = initialDomain<T>(),
  action: ActionTypes<T>,
): NormalizedState<T> =>
  resetReducer<NormalizedState<T>>(state, action, initialDomain<T>());

const resetStateOnLogoutReducer = <S extends Identifiable>(
  state: NormalizedState<S> = initialDomain<S>(),
  {type}: ActionTypes<S>,
): NormalizedState<S> => {
  switch (type) {
    case getType(logoutUser):
      return initialDomain<S>();
    default:
      return state;
  }
};

export const meters = reducerFor<MeterDetails>(
  'meters',
  EndPoints.meters,
  resetStateReducer,
);

export const users = reducerFor<User>(
  'users',
  EndPoints.users,
  resetStateReducer,
);

export const meterMapMarkers = reducerFor<MapMarker>(
  'meterMapMarkers',
  EndPoints.meterMapMarkers,
  resetStateReducer,
);

export const gatewayMapMarkers = reducerFor<MapMarker>(
  'gatewayMapMarkers',
  EndPoints.gatewayMapMarkers,
  resetStateReducer,
);

export const organisations = reducerFor<Organisation>(
  'organisations',
  EndPoints.organisations,
  resetStateReducer,
);

export const meterDefinitions = reducerFor<MeterDefinition>(
  'meterDefinitions',
  EndPoints.meterDefinitions,
  resetStateReducer,
);

export const quantities = reducerFor<Quantity>(
  'quantities',
  EndPoints.quantities,
  resetStateReducer,
);

export const mediums = reducerFor<Medium>(
  'mediums',
  EndPoints.mediums,
  resetStateReducer,
);

export const userSelections = reducerFor<UserSelection>(
  'userSelections',
  EndPoints.userSelections,
  resetStateOnLogoutReducer
);

export const collectionStats = reducerFor<CollectionStat>(
  'collectionStats',
  EndPoints.collectionStatDate,
  resetStateReducer
);

export const domainModels = combineReducers<DomainModelsState>({
  gatewayMapMarkers,
  meters,
  meterDetailMeasurement,
  meterMapMarkers,
  organisations,
  meterDefinitions,
  quantities,
  userSelections,
  users,
  collectionStats,
  mediums,
});

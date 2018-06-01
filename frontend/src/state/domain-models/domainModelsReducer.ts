import {EmptyAction} from 'react-redux-typescript';
import {combineReducers} from 'redux';
import {EndPoints} from '../../services/endPoints';
import {Action, ErrorResponse, Identifiable, uuid} from '../../types/Types';
import {LOGOUT_USER} from '../../usecases/auth/authActions';
import {MapMarker} from '../../usecases/map/mapModels';
import {
  ADD_PARAMETER_TO_SELECTION,
  DESELECT_SELECTION,
  RESET_SELECTION,
  SELECT_PERIOD,
  SELECT_SAVED_SELECTION,
  SET_CUSTOM_DATE_RANGE,
} from '../user-selection/userSelectionActions';
import {UserSelection} from '../user-selection/userSelectionModels';
import {
  DomainModelsState,
  Normalized,
  NormalizedState,
  ObjectsById,
  SelectionEntity,
} from './domainModels';
import {
  domainModelsClearError,
  domainModelsDeleteSuccess,
  domainModelsFailure,
  domainModelsGetEntitySuccess,
  domainModelsGetSuccess,
  domainModelsPostSuccess,
  domainModelsPutSuccess,
  domainModelsRequest,
} from './domainModelsActions';
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
  {payload}: Action<Normalized<T>>,
): NormalizedState<T> => {
  const entities: ObjectsById<T> = entity in payload.entities ? payload.entities[entity] : {};
  if (Array.isArray(payload.result)) {
    const {result} = payload;
    return {
      ...state,
      isFetching: false,
      isSuccessfullyFetched: true,
      entities,
      result,
      total: result.length,
    };
  } else {
    const result = Object.keys(entities);
    return {
      ...state,
      isFetching: false,
      isSuccessfullyFetched: true,
      entities,
      result,
      total: result.length,
    };
  }
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
        return {
          ...state,
          isFetching: true,
        };
      case domainModelsGetSuccess(endPoint):
        return setEntities<T>(entity, state, action as Action<Normalized<T>>);
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
      case domainModelsClearError(endPoint):
      case LOGOUT_USER:
        return {...initialDomain<T>()};
      default:
        return resetState(state, action, endPoint);
    }
  };

const identity = (state, action, endPoint) => state;

const resetStateReducer = <T extends Identifiable>(
  state: NormalizedState<T> = initialDomain<T>(),
  action: ActionTypes<T>,
): NormalizedState<T> =>
  resetReducer<NormalizedState<T>>(state, action, {...initialDomain<T>()});

export const resetReducer = <S>(
  state: S,
  {type}: EmptyAction<string>,
  initialState: S,
): S => {
  switch (type) {
    case SELECT_SAVED_SELECTION:
    case ADD_PARAMETER_TO_SELECTION:
    case DESELECT_SELECTION:
    case RESET_SELECTION:
    case SELECT_PERIOD:
    case SET_CUSTOM_DATE_RANGE:
      return initialState;
    default:
      return state;
  }
};

export const countries = reducerFor<SelectionEntity>('countries', EndPoints.selections);
export const cities = reducerFor<SelectionEntity>('cities', EndPoints.selections);
export const addresses = reducerFor<SelectionEntity>('addresses', EndPoints.selections);
export const alarms = reducerFor<SelectionEntity>('alarms', EndPoints.selections);
export const gatewayStatuses = reducerFor<SelectionEntity>('gatewayStatuses', EndPoints.selections);
export const media = reducerFor<SelectionEntity>('media', EndPoints.selections);
export const meterStatuses = reducerFor<SelectionEntity>('meterStatuses', EndPoints.selections);

export const meters = reducerFor<MeterDetails>('meters', EndPoints.meters, resetStateReducer);
export const users = reducerFor<User>('users', EndPoints.users, resetStateReducer);

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

export const userSelections = reducerFor<UserSelection>('userSelections', EndPoints.userSelections);

export const domainModels = combineReducers<DomainModelsState>({
  addresses,
  alarms,
  cities,
  countries,
  gatewayMapMarkers,
  gatewayStatuses,
  media,
  meters,
  meterMapMarkers,
  meterStatuses,
  organisations,
  userSelections,
  users,
});

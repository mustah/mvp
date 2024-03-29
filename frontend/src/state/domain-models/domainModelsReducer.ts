import {combineReducers} from 'redux';
import {getType} from 'typesafe-actions';
import {EmptyAction} from 'typesafe-actions/dist/type-helpers';
import {resetReducer} from '../../reducers/resetReducer';
import {EndPoints} from '../../services/endPoints';
import {Action, ActionKey, ErrorResponse, Identifiable, Sectors, uuid} from '../../types/Types';
import {logoutUser} from '../../usecases/auth/authActions';
import {setMeterCollectionStatsTimePeriod} from '../../usecases/collection/collectionActions';
import {MapMarker} from '../../usecases/map/mapModels';
import {meterDetailMeasurementRequest} from '../../usecases/meter/measurements/meterDetailMeasurementActions';
import {meterDetailMeasurement} from '../../usecases/meter/measurements/meterDetailMeasurementReducer';
import {LegendDto} from '../report/reportModels';
import {search} from '../search/searchActions';
import {QueryParameter} from '../search/searchModels';
import {UserSelection} from '../user-selection/userSelectionModels';
import {CollectionStat} from './collection-stat/collectionStatModels';
import {Dashboard} from './dashboard/dashboardModels';
import {DomainModelsState, Normalized, NormalizedState, ObjectsById} from './domainModels';
import {
  domainModelsClear,
  domainModelsClearError,
  domainModelsDeleteSuccess,
  domainModelsFailure,
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
import {Widget} from './widget/widgetModels';

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
  | Action<QueryParameter>
  | Action<T>
  | Action<ErrorResponse>;

const reducerFor = <T extends Identifiable>(
  entity: keyof DomainModelsState,
  actionKey: ActionKey,
  resetState = identity,
) =>
  (
    state: NormalizedState<T> = initialDomain<T>(),
    action: ActionTypes<T>,
  ): NormalizedState<T> => {
    switch (action.type) {
      case domainModelsRequest(actionKey):
        return {...state, isFetching: true};
      case domainModelsGetSuccess(actionKey):
        return setEntities<T>(entity, state, (action as Action<Normalized<T>>).payload);
      case domainModelsGetEntitySuccess(actionKey):
        return addEntity<T>(state, action as Action<T>);
      case domainModelsPostSuccess(actionKey):
        return addEntity<T>(state, action as Action<T>);
      case domainModelsPutSuccess(actionKey):
        return modifyEntity<T>(state, action as Action<T>);
      case domainModelsDeleteSuccess(actionKey):
        return removeEntity<T>(state, action as Action<T>);
      case domainModelsFailure(actionKey):
        return setError(state, action as Action<ErrorResponse>);
      case domainModelsClearError(actionKey):
      case domainModelsClear(actionKey):
      case getType(search):
        return initialDomain<T>();
      default:
        return resetState(state, action, actionKey);
    }
  };

const identity = (state, _, __) => state;

const resetStateReducer = <T extends Identifiable>(
  state: NormalizedState<T> = initialDomain<T>(),
  action: ActionTypes<T>,
): NormalizedState<T> =>
  resetReducer<NormalizedState<T>>(state, action, initialDomain<T>());

export const resetStateOnLogoutReducer = <S extends Identifiable>(
  state: NormalizedState<S> = initialDomain<S>(),
  {type}: ActionTypes<S>,
): NormalizedState<S> =>
  type === getType(logoutUser) ? initialDomain<S>() : state;

const resetMeterCollectionStatsReducer = <T extends Identifiable>(
  state: NormalizedState<T> = initialDomain<T>(),
  action: ActionTypes<T>,
): NormalizedState<T> => {
  switch (action.type) {
    case getType(setMeterCollectionStatsTimePeriod):
    case getType(meterDetailMeasurementRequest):
      return initialDomain<T>();
    default:
      return resetReducer<NormalizedState<T>>(state, action, initialDomain<T>());
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

export const subOrganisations = reducerFor<Organisation>(
  'organisations',
  Sectors.subOrganisations,
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

export const allCollectionStats = reducerFor<CollectionStat>(
  'allCollectionStats',
  EndPoints.collectionStats,
  resetStateReducer
);

export const collectionStats = reducerFor<CollectionStat>(
  'collectionStats',
  Sectors.collection,
  resetStateReducer
);

export const meterCollectionStats = reducerFor<CollectionStat>(
  'collectionStats',
  Sectors.meterCollection,
  resetMeterCollectionStatsReducer
);

export const dashboards = reducerFor<Dashboard>(
  'dashboards',
  EndPoints.dashboard,
  resetStateOnLogoutReducer
);

export const widgets = reducerFor<Widget>(
  'widgets',
  EndPoints.widgets,
  resetStateOnLogoutReducer
);

export const legendItems = reducerFor<LegendDto>(
  'legendItems',
  EndPoints.legendItems,
  resetStateReducer
);

export const domainModels = combineReducers<DomainModelsState>({
  allCollectionStats,
  collectionStats,
  dashboards,
  gatewayMapMarkers,
  legendItems,
  mediums,
  meterCollectionStats,
  meters,
  meterDetailMeasurement,
  meterMapMarkers,
  meterDefinitions,
  organisations,
  quantities,
  subOrganisations,
  userSelections,
  users,
  widgets,
});

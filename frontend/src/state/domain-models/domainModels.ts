import {ErrorResponse, Identifiable, uuid} from '../../types/Types';
import {MapMarker} from '../../usecases/map/mapModels';
import {LegendDto} from '../report/reportModels';
import {MeasurementState} from '../ui/graph/measurement/measurementModels';
import {UserSelection} from '../user-selection/userSelectionModels';
import {CollectionStat} from './collection-stat/collectionStatModels';
import {Dashboard} from './dashboard/dashboardModels';
import {Medium, MeterDefinition, Quantity} from './meter-definitions/meterDefinitionModels';
import {MeterDetails} from './meter-details/meterDetailsModels';
import {Organisation} from './organisation/organisationModels';
import {UserState} from './user/userModels';
import {Widget} from './widget/widgetModels';

export interface ObjectsById<T extends Identifiable> {
  [id: string]: T;
}

export interface Normalized<T extends Identifiable> {
  result: uuid[];
  entities: {
    [entityType: string]: ObjectsById<T>,
  };
}

export interface DomainModel<T extends Identifiable> {
  entities: ObjectsById<T>;
  result: uuid[];
}

export interface RequestsHttp {
  isFetching: boolean;
  isSuccessfullyFetched: boolean;
  error?: ErrorResponse;
}

export interface NormalizedState<T extends Identifiable> extends DomainModel<T>, RequestsHttp {
  total: number;
}

export interface DomainModelsState {
  allCollectionStats: NormalizedState<CollectionStat>;
  collectionStats: NormalizedState<CollectionStat>;
  dashboards: NormalizedState<Dashboard>;
  gatewayMapMarkers: NormalizedState<MapMarker>;
  legendItems: NormalizedState<LegendDto>;
  meterMapMarkers: NormalizedState<MapMarker>;
  meters: NormalizedState<MeterDetails>;
  meterDefinitions: NormalizedState<MeterDefinition>;
  mediums: NormalizedState<Medium>;
  meterCollectionStats: NormalizedState<CollectionStat>;
  meterDetailMeasurement: MeasurementState;
  organisations: NormalizedState<Organisation>;
  quantities: NormalizedState<Quantity>;
  subOrganisations: NormalizedState<Organisation>;
  userSelections: NormalizedState<UserSelection>;
  users: UserState;
  widgets: NormalizedState<Widget>;
}

export const enum RequestType {
  GET = 'GET',
  GET_ENTITY = 'GET_ENTITY',
  POST = 'POST',
  PUT = 'PUT',
  DELETE = 'DELETE',
}

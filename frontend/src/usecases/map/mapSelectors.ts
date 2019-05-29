import {createSelector} from 'reselect';
import {identity} from '../../helpers/commonHelpers';
import {Maybe} from '../../helpers/Maybe';
import {RootState} from '../../reducers/rootReducer';
import {DomainModel, Normalized, NormalizedState, ObjectsById} from '../../state/domain-models/domainModels';
import {getDomainModel} from '../../state/domain-models/domainModelsSelectors';
import {uuid} from '../../types/Types';
import {makeLeafletCompatibleMarkersFrom} from './helper/clusterHelper';
import {boundsFromMarkers, gatewayLowConfidenceTextInfo, meterLowConfidenceTextInfo} from './helper/mapHelper';
import {Bounds, MapMarker, MapMarkerClusters, MapZoomSettings} from './mapModels';
import {MapState} from './mapReducer';

export const getBounds =
  createSelector<DomainModel<MapMarker>, DomainModel<MapMarker>, Bounds>(
    getDomainModel,
    ({entities}: DomainModel<MapMarker>) => boundsFromMarkers(entities),
  );

const getTotalMeters = (state: RootState): number => state.summary.payload.numMeters;
const getMeterMapMarkers = (state: RootState) => state.domainModels.meterMapMarkers;
const getGatewayMapMarkers = (state: RootState) => state.domainModels.gatewayMapMarkers;
const getValidationQuery = (state: RootState): string | undefined => state.search.validation.query;

const totalMapMarkers = (markers: NormalizedState<MapMarker>): number => markers.total;

const getTotalMeterMapMarkers =
  createSelector<RootState, NormalizedState<MapMarker>, number>(
    getMeterMapMarkers,
    totalMapMarkers,
  );

const getTotalGatewayMapMarkers =
  createSelector<RootState, NormalizedState<MapMarker>, number>(
    getGatewayMapMarkers,
    totalMapMarkers,
  );

export const getMeterLowConfidenceTextInfo =
  createSelector<RootState, string | undefined, number, number, string | undefined>(
    getValidationQuery,
    getTotalMeters,
    getTotalMeterMapMarkers,
    meterLowConfidenceTextInfo,
  );

export const getGatewayLowConfidenceTextInfo =
  createSelector<RootState, number, number, string | undefined>(
    getTotalMeters,
    getTotalGatewayMapMarkers,
    gatewayLowConfidenceTextInfo,
  );

export const getMapZoomSettings = (id: uuid) =>
  createSelector<MapState, MapZoomSettings | undefined, Partial<MapZoomSettings>>(
    state => state[id],
    settings => Maybe.maybe<MapZoomSettings>(settings).map(identity).orElse({})
  );

export const getMapMarkers = createSelector<NormalizedState<MapMarker>, ObjectsById<MapMarker>, MapMarkerClusters>(
  state => getDomainModel(state).entities,
  mapMarkers => makeLeafletCompatibleMarkersFrom(mapMarkers || {})
);

export const getWidgetMapMarkers = createSelector<Normalized<MapMarker>, ObjectsById<MapMarker>, MapMarkerClusters>(
  ({entities: {meterMapMarkers}}) => meterMapMarkers,
  mapMarkers => makeLeafletCompatibleMarkersFrom(mapMarkers || {})
);

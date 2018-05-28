import {EndPoints} from '../../services/endPoints';
import {clearError, fetchIfNeeded} from '../../state/domain-models/domainModelsActions';
import {mapMarkerSchema} from './gatewayMapMarkerSchema';
import {MapMarker} from './mapModels';

export const clearErrorGatewayMapMarkers = clearError(EndPoints.gatewayMapMarkers);

export const fetchGatewayMapMarkers = fetchIfNeeded<MapMarker>(
  EndPoints.gatewayMapMarkers,
  mapMarkerSchema,
  'gatewayMapMarkers',
);

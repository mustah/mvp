import {EndPoints} from '../../services/endPoints';
import {clearError, fetchEntityIfNeeded, fetchIfNeeded} from '../../state/domain-models/domainModelsActions';
import {gatewayMapMarkersDataFormatter, meterMapMarkersDataFormatter} from './mapMarkerSchema';
import {MapMarker} from './mapModels';

export const clearErrorGatewayMapMarkers = clearError(EndPoints.gatewayMapMarkers);

export const clearErrorMeterMapMarkers = clearError(EndPoints.meterMapMarkers);

export const fetchGatewayMapMarkers = fetchIfNeeded<MapMarker>(
  EndPoints.gatewayMapMarkers,
  'gatewayMapMarkers',
  gatewayMapMarkersDataFormatter,
);

export const fetchMeterMapMarkers = fetchIfNeeded<MapMarker>(
  EndPoints.meterMapMarkers,
  'meterMapMarkers',
  meterMapMarkersDataFormatter,
);

export const fetchMeterMapMarker = fetchEntityIfNeeded<MapMarker>(
  EndPoints.meterMapMarkers,
  'meterMapMarkers'
);

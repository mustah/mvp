import {EndPoints} from '../../services/endPoints';
import {
  clearError,
  fetchEntityIfNeeded,
  fetchIfNeeded,
} from '../../state/domain-models/domainModelsActions';
import {MapMarker} from './mapModels';
import {mapMarkerSchema} from './meterMapMarkerSchema';

export const clearErrorMeterMapMarkers = clearError(EndPoints.meterMapMarkers);

export const fetchMeterMapMarkers = fetchIfNeeded<MapMarker>(
  EndPoints.meterMapMarkers,
  mapMarkerSchema,
  'meterMapMarkers',
);

export const fetchMeterMapMarker = fetchEntityIfNeeded<MapMarker>(
  EndPoints.meterMapMarkers,
  'meterMapMarkers',
);

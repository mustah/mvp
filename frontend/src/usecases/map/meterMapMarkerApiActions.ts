import {EndPoints} from '../../services/endPoints';
import {clearError, fetchIfNeeded} from '../../state/domain-models/domainModelsActions';
import {mapMarkerSchema} from './meterMapMarkerSchema';

export const clearErrorMeterMapMarkers = clearError(EndPoints.meterMapMarkers);
export const fetchMeterMapMarkers = fetchIfNeeded(EndPoints.meterMapMarkers, mapMarkerSchema, 'meterMapMarkers');

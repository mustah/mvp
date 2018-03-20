import {EndPoints} from '../../services/endPoints';
import {clearError, fetchIfNeeded} from '../../state/domain-models/domainModelsActions';
import {mapMarkerSchema} from './gatewayMapMarkerSchema';

export const fetchGatewayMapMarkers = fetchIfNeeded(EndPoints.gatewayMapMarkers, mapMarkerSchema, 'gatewayMapMarkers');
export const  clearErrorGatewayMapMarkers = clearError(EndPoints.gatewayMapMarkers);

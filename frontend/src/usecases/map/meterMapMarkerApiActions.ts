import {EndPoints} from '../../services/endPoints';
import {fetchIfNeeded} from '../../state/domain-models/domainModelsActions';
import {mapMarkerSchema} from './meterMapMarkerSchema';

export const fetchMeterMapMarkers = fetchIfNeeded(EndPoints.meterMapMarkers, mapMarkerSchema, 'meterMapMarkers');

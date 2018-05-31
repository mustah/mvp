import {normalize, Schema, schema} from 'normalizr';
import {DataFormatter} from '../../state/domain-models/domainModelsActions';
import {flatMapMarkers} from './helper/mapHelper';
import {MapMarkerApiResponse} from './mapModels';

const gatewayMapMarkerSchema: Schema = [new schema.Entity('gatewayMapMarkers')];

const meterMapMarkerSchema: Schema = [new schema.Entity('meterMapMarkers')];

const normalizer: DataFormatter = (schema: Schema) =>
  (response: MapMarkerApiResponse) => normalize(flatMapMarkers(response), schema);

export const gatewayMapMarkersDataFormatter: DataFormatter = normalizer(gatewayMapMarkerSchema);

export const meterMapMarkersDataFormatter: DataFormatter = normalizer(meterMapMarkerSchema);

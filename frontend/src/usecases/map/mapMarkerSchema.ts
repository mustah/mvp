import {normalize, Schema, schema} from 'normalizr';
import {Normalized} from '../../state/domain-models/domainModels';
import {DataFormatter} from '../../state/domain-models/domainModelsActions';
import {flatMapMarkers} from './helper/mapHelper';
import {MapMarker, MapMarkerApiResponse} from './mapModels';

const gatewayMapMarkerSchema: Schema = [new schema.Entity('gatewayMapMarkers')];

const meterMapMarkerSchema: Schema = [new schema.Entity('meterMapMarkers')];

const normalizer = (schema: Schema): DataFormatter<Normalized<MapMarker>> =>
  (response: MapMarkerApiResponse): Normalized<MapMarker> =>
    normalize(flatMapMarkers(response), schema);

export const gatewayMapMarkersDataFormatter: DataFormatter<Normalized<MapMarker>> =
  normalizer(gatewayMapMarkerSchema);

export const meterMapMarkersDataFormatter: DataFormatter<Normalized<MapMarker>> =
  normalizer(meterMapMarkerSchema);

import {Schema, schema} from 'normalizr';

const mapMarker = new schema.Entity('gatewayMapMarkers');

export const mapMarkerSchema: Schema = [mapMarker];

import {Schema, schema} from 'normalizr';

const mapMarker = new schema.Entity('meterMapMarkers');

export const mapMarkerSchema: Schema = [mapMarker];

import {Icon, LatLngTuple, MarkerOptions} from 'leaflet';
import {Status, uuid} from '../../types/Types';

export interface MapMarker {
  id: uuid;
  mapMarkerType: 'Meter' | 'Gateway';
  status: Status;
  latitude: number;
  longitude: number;
  confidence: number;
}

export interface Marker {
  position: LatLngTuple;
  options: MarkerOptions & {
    icon: Icon;
    mapMarkerItem: uuid;
  };
}

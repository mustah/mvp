import {Icon, LatLngTuple, MarkerOptions} from 'leaflet';
import {Identifiable, Status, uuid} from '../../types/Types';

export interface MapMarker extends Identifiable {
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

import {Icon, LatLngTuple, MarkerOptions} from 'leaflet';
import {GeoPosition} from '../../state/domain-models/location/locationModels';
import {Dictionary, Identifiable, Status, uuid} from '../../types/Types';

export type IdentifiablePosition = Identifiable & GeoPosition;

export interface MapMarkerApiResponse {
  markers: Dictionary<IdentifiablePosition[]>;
}

export interface MapMarker extends Identifiable {
  status: Status;
  latitude: number;
  longitude: number;
}

export interface Marker {
  position: LatLngTuple;
  options: MarkerOptions & {
    icon: Icon;
    mapMarkerItem: uuid;
  };
}

export type Bounds = LatLngTuple[] | undefined;

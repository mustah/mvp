import {IdNamed} from '../../../types/Types';

export interface Location {
  address: IdNamed;
  city: IdNamed;
  position: GeoPosition;
}

export interface GeoPosition {
  latitude: number;
  longitude: number;
}

export interface City extends IdNamed {
  country: IdNamed;
}

export interface Address extends IdNamed {
  city: IdNamed;
  country: IdNamed;
}

export interface LocationHolder {
  location: Location;
}

import {IdNamed, uuid} from '../../types/Types';

export interface Location {
  // TODO use type Address
  address: string;
  city: string;
  position: GeoPosition;
}

export interface GeoPosition {
  latitude: string;
  longitude: string;
  confidence: number;
}

export interface Address extends IdNamed {
  cityId: uuid;
}

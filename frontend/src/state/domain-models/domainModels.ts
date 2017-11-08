import {IdNamed, uuid} from '../../types/Types';

export interface Location {
  address: Address;
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

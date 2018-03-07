import {IdNamed, uuid} from '../../../types/Types';

export interface Location {
  address: IdNamed;
  city: IdNamed;
  position: GeoPosition;
}

export interface GeoPosition {
  latitude: number;
  longitude: number;
  confidence: number;
}

export interface Address extends IdNamed {
  parentId: uuid;
}

export interface LocationHolder {
  location: Location;
}

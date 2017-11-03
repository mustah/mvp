export interface MappedObject extends Location {
  status: string;
}

export interface Location {
  address: string;
  city: string;
  position: GeoPosition;
}

export interface GeoPosition {
  latitude: string;
  longitude: string;
  confidence: number;
}

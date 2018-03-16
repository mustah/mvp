import * as Leaflet from 'leaflet';
import {GeoPosition} from '../../../state/domain-models/location/locationModels';
import {Dictionary, IdNamed} from '../../../types/Types';
import {MapMarker, MapMarkerItem, Marker} from '../mapModels';

const icons = {
  ok: 'assets/images/marker-icon-ok.png',
  warning: 'assets/images/marker-icon-warning.png',
  critical: 'assets/images/marker-icon-error.png',
};

const getStatusIcon = (status: IdNamed): string => status
  ? icons[status.id] || 'assets/images/marker-icon.png'
  : 'assets/images/marker-icon.png';

const toLatLngTuple = ({latitude, longitude}: GeoPosition): Leaflet.LatLngTuple =>
  [latitude, longitude];

const makeMarker = (marker: MapMarker): Marker => ({
  position: toLatLngTuple(marker.location.position),
  options: {
    icon: Leaflet.icon({iconUrl: getStatusIcon(marker.status)}),
    mapMarkerItem: marker as MapMarkerItem,
  },
});

export const isMarkersWithinThreshold = (markers: Dictionary<MapMarker>): boolean => {
  return markers && Object.keys(markers)
    .map((key: string) => markers[key])
    .filter(isGeoPositionWithinThreshold)
    .length >= 0;
};

export const isGeoPositionWithinThreshold =
  ({location: {position: {latitude, longitude, confidence}}}: MapMarker) =>
    latitude !== undefined && longitude !== undefined && confidence >= 0.7;

export const isMapMarker = (markers: Dictionary<MapMarker> | MapMarker): markers is MapMarker =>
  (markers as MapMarker).status !== undefined &&
  (markers as MapMarker).location.position !== undefined;

export const makeLeafletCompatibleMarkersFrom = (markers: Dictionary<MapMarker> | MapMarker): Marker[] => {
  const mapMarkers = isMapMarker(markers) ? {markers} : markers;
  return Object.keys(mapMarkers)
    .map((key: string) => mapMarkers[key])
    .filter(isGeoPositionWithinThreshold)
    .map(makeMarker);
};

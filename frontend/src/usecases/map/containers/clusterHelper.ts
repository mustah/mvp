import * as Leaflet from 'leaflet';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {IdNamed} from '../../../types/Types';
import {MapMarker, MapMarkerItem, Marker} from '../mapModels';

const icons = {
  0: 'assets/images/marker-icon-ok.png',
  1: 'assets/images/marker-icon-ok.png',
  2: 'assets/images/marker-icon-warning.png',
  3: 'assets/images/marker-icon-error.png',
};

const getStatusIcon = ({id}: IdNamed): string => icons[id] || 'assets/images/marker-icon.png';

export const isMarkersWithinThreshold = (markers: DomainModel<MapMarker>): boolean => {
  return markers && Object.keys(markers)
    .map((key: string) => markers[key])
    .filter(isGeoPositionWithinThreshold)
    .length >= 0;
};

export const isGeoPositionWithinThreshold = ({position: {latitude, longitude, confidence}}: MapMarker) =>
  latitude && longitude && confidence >= 0.7;

const makeMarker = (marker: MapMarker): Marker => ({
  position: [marker.position.latitude, marker.position.longitude] as Leaflet.LatLngTuple,
  options: {
    icon: Leaflet.icon({iconUrl: getStatusIcon(marker.status)}),
    mapMarkerItem: marker as MapMarkerItem,
  },
});

export const isMapMarker = (markers: DomainModel<MapMarker> | MapMarker): markers is MapMarker =>
  (markers as MapMarker).status !== undefined &&
  (markers as MapMarker).position !== undefined;

export const makeLeafletCompatibleMarkersFrom = (markers: DomainModel<MapMarker> | MapMarker): Marker[] => {
  const mapMarkers = isMapMarker(markers) ? {markers} : markers;
  return Object.keys(mapMarkers)
    .map((key: string) => mapMarkers[key])
    .filter(isGeoPositionWithinThreshold)
    .map(makeMarker);
};
import * as Leaflet from 'leaflet';
import {assetsPathFor} from '../../../app/routes';
import {Dictionary, Status} from '../../../types/Types';
import {MapMarker, Marker} from '../mapModels';

// TODO: Check if more markers types than 3 are needed to distinguish the different statuses
const icons = {
  [Status.ok]: assetsPathFor('marker-icon-ok.png'),
  [Status.active]: assetsPathFor('marker-icon-ok.png'),
  [Status.warning]: assetsPathFor('marker-icon-warning.png'),
  [Status.info]: assetsPathFor('marker-icon-warning.png'),
  [Status.maintenance_scheduled]: assetsPathFor('marker-icon-warning.png'),
  [Status.alarm]: assetsPathFor('marker-icon-error.png'),
  [Status.critical]: assetsPathFor('marker-icon-error.png'),
};

const getStatusIcon = (status: Status): string =>
  status ? icons[status] || assetsPathFor('marker-icon.png') : assetsPathFor('marker-icon.png');

const makeMarker = (marker: MapMarker): Marker => ({
  position: [marker.latitude, marker.longitude],
  options: {
    icon: Leaflet.icon({iconUrl: getStatusIcon(marker.status)}),
    mapMarkerItem: marker.id,
  },
});

export const metersWithinThreshold = (markers: Dictionary<MapMarker>): MapMarker[] =>
  markers
    ? Object.keys(markers)
      .map((key: string) => markers[key])
      .filter(isGeoPositionWithinThreshold)
    : [];

export const isGeoPositionWithinThreshold =
  ({latitude, longitude, confidence}: MapMarker) =>
    latitude !== undefined && longitude !== undefined && confidence >= 0.7;

export const isMapMarker = (markers: Dictionary<MapMarker> | MapMarker): markers is MapMarker =>
  (markers as MapMarker).status !== undefined &&
  (markers as MapMarker).longitude !== undefined &&
  (markers as MapMarker).latitude !== undefined;

export const makeLeafletCompatibleMarkersFrom = (markers: Dictionary<MapMarker> | MapMarker): Marker[] => {
  const mapMarkers = isMapMarker(markers) ? {markers} : markers;
  return Object.keys(mapMarkers)
    .map((key: string) => mapMarkers[key])
    .filter(isGeoPositionWithinThreshold)
    .map(makeMarker);
};

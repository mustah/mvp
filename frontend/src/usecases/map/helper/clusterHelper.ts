import * as Leaflet from 'leaflet';
import {imagePathFor} from '../../../app/routes';
import {Dictionary, Status} from '../../../types/Types';
import {MapMarker, Marker} from '../mapModels';
import {isGeoPositionWithinThreshold} from './mapHelper';

const icons = {
  [Status.ok]: imagePathFor('marker-icon-ok.png'),
  [Status.active]: imagePathFor('marker-icon-ok.png'),
  [Status.warning]: imagePathFor('marker-icon-warning.png'),
  [Status.info]: imagePathFor('marker-icon-warning.png'),
  [Status.maintenance_scheduled]: imagePathFor('marker-icon-warning.png'),
  [Status.alarm]: imagePathFor('marker-icon-error.png'),
  [Status.critical]: imagePathFor('marker-icon-error.png'),
};

const getStatusIcon = (status: Status): string => icons[status] || imagePathFor('marker-icon.png');

const makeMarker = (marker: MapMarker): Marker => ({
  position: [marker.latitude, marker.longitude],
  options: {
    icon: Leaflet.icon({iconUrl: getStatusIcon(marker.status)}),
    mapMarkerItem: marker.id,
  },
});

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
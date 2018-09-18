import * as Leaflet from 'leaflet';
import {imagePathFor} from '../../../app/routes';
import {Dictionary, Status} from '../../../types/Types';
import {MapMarker, Marker} from '../mapModels';

const icons = {
  [Status.ok]: imagePathFor('marker-icon-ok.png'),
  [Status.warning]: imagePathFor('marker-icon-warning.png'),
  [Status.error]: imagePathFor('marker-icon-error.png'),
  [Status.unknown]: imagePathFor('marker-icon-error.png'),
  alarm: imagePathFor('marker-icon-alarm.png'),
};

const markerIcon = imagePathFor('marker-icon.png');
const alarmMarkerIcon = icons.alarm;
const errorMarkerIcon = icons[Status.error];
const warningMarkerIcon = icons[Status.warning];

const getStatusIconUrl = ({alarm, status}: MapMarker): string =>
  alarm !== undefined ? alarmMarkerIcon : (icons[status] || markerIcon);

const makeMarker = (marker: MapMarker): Marker => ({
  position: [marker.latitude, marker.longitude],
  options: {
    icon: Leaflet.icon({iconUrl: getStatusIconUrl(marker)}),
    mapMarkerItem: marker.id,
  },
});

export const isAlarmIconUrl = (iconUrl?: string): boolean => iconUrl === alarmMarkerIcon;
export const isErrorIconUrl = (iconUrl?: string): boolean => iconUrl === errorMarkerIcon;
export const isWarningIconUrl = (iconUrl?: string): boolean => iconUrl === warningMarkerIcon;

export const isMapMarker = (markers: Dictionary<MapMarker> | MapMarker): markers is MapMarker =>
  (markers as MapMarker).status !== undefined &&
  (markers as MapMarker).longitude !== undefined &&
  (markers as MapMarker).latitude !== undefined;

export const makeLeafletCompatibleMarkersFrom = (markers: Dictionary<MapMarker> | MapMarker): Marker[] => {
  const mapMarkers = isMapMarker(markers) ? {markers} : markers;
  return Object.keys(mapMarkers)
    .map((key: string) => mapMarkers[key])
    .map(makeMarker);
};

import * as Leaflet from 'leaflet';
import markerIconAlarm from '../../../assets/images/marker-icon-alarm.png';
import markerIconError from '../../../assets/images/marker-icon-error.png';
import markerIconOk from '../../../assets/images/marker-icon-ok.png';
import markerIconWarning from '../../../assets/images/marker-icon-warning.png';
import markerIcon from '../../../assets/images/marker-icon.png';
import {Dictionary, Status} from '../../../types/Types';
import {MapMarker, MapMarkerClusters, Marker} from '../mapModels';

const icons = {
  [Status.ok]: markerIconOk,
  [Status.warning]: markerIconWarning,
  [Status.error]: markerIconError,
  [Status.unknown]: markerIconError,
  alarm: markerIconAlarm,
};

const alarmMarkerIcon = icons.alarm;
const errorMarkerIcon = icons[Status.error];
const warningMarkerIcon = icons[Status.warning];

const getStatusIconUrl = ({alarm, status}: MapMarker): string =>
  alarm !== undefined ? alarmMarkerIcon : (icons[status] || markerIcon);

const iconSize: Leaflet.PointTuple = [25, 41];

const centerBottom = ([x, y]: Leaflet.PointTuple): Leaflet.Point => Leaflet.point(x / 2, y, true);

const makeMarker = (marker: MapMarker): Marker => ({
  position: [marker.latitude, marker.longitude],
  options: {
    icon: Leaflet.icon({
      iconUrl: getStatusIconUrl(marker),
      iconSize,
      iconAnchor: centerBottom(iconSize),
    }),
    mapMarkerItem: marker.id,
    status: marker.status,
  },
});

export const isMapMarker = (markers: Dictionary<MapMarker> | MapMarker): markers is MapMarker =>
  (markers as MapMarker).status !== undefined &&
  (markers as MapMarker).longitude !== undefined &&
  (markers as MapMarker).latitude !== undefined;

const nonOkIconUrl = (iconUrl?: string): boolean =>
  iconUrl === warningMarkerIcon || iconUrl === errorMarkerIcon || iconUrl === alarmMarkerIcon;

export const emptyClusters: MapMarkerClusters = {markers: [], faults: 0};

export const makeLeafletCompatibleMarkersFrom = (markers: Dictionary<MapMarker> | MapMarker): MapMarkerClusters => {
  const mapMarkers = isMapMarker(markers) ? {markers} : markers;

  const result: Marker[] = [];
  let faults = 0;
  Object.keys(mapMarkers).forEach((mapMarker) => {
    const possibleMarker = mapMarkers[mapMarker];

    if (isMapMarker(possibleMarker)) {
      const marker = makeMarker(possibleMarker);
      result.push(marker);

      if (nonOkIconUrl(marker.options.icon.options.iconUrl)) {
        faults++;
      }
    }
  });

  return {markers: result, faults};
};

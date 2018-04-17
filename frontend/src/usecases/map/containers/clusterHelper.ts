import * as Leaflet from 'leaflet';
import {imagePathFor} from '../../../app/routes';
import {Maybe} from '../../../helpers/Maybe';
import {Dictionary, Status} from '../../../types/Types';
import {MapMarker, Marker} from '../mapModels';

// TODO: Check if more markers types than 3 are needed to distinguish the different statuses
const icons = {
  [Status.ok]: imagePathFor('marker-icon-ok.png'),
  [Status.active]: imagePathFor('marker-icon-ok.png'),
  [Status.warning]: imagePathFor('marker-icon-warning.png'),
  [Status.info]: imagePathFor('marker-icon-warning.png'),
  [Status.maintenance_scheduled]: imagePathFor('marker-icon-warning.png'),
  [Status.alarm]: imagePathFor('marker-icon-error.png'),
  [Status.critical]: imagePathFor('marker-icon-error.png'),
};

const getStatusIcon = (status: Status): string =>
  status ? icons[status] || imagePathFor('marker-icon.png') : imagePathFor('marker-icon.png');

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
    latitude !== undefined && longitude !== undefined && confidence >= 0.75;

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

interface MarkerBounds {
  minLat: number;
  maxLat: number;
  minLong: number;
  maxLong: number;
}

export const boundsFromMarkers = (markers: Dictionary<MapMarker>): Maybe<Leaflet.LatLngTuple[]> => {
  const filteredMarkers = metersWithinThreshold(markers);

  const bounds = Object.keys(filteredMarkers)
    .reduce(
      (sum: MarkerBounds, markerId: string) => {
        const {latitude, longitude} = filteredMarkers[markerId];

        if (!isNaN(latitude)) {
          if (latitude < sum.minLat) {
            sum.minLat = latitude;
          } else if (latitude > sum.maxLat) {
            sum.maxLat = latitude;
          }
        }

        if (!isNaN(longitude)) {
          if (longitude < sum.minLong) {
            sum.minLong = longitude;
          } else if (longitude > sum.maxLong) {
            sum.maxLong = longitude;
          }
        }

        return sum;
      },
      {
        minLat: 9999,
        maxLat: -9999,
        minLong: 9999,
        maxLong: -9999,
      },
    );

  const changedBounds = Object.keys(bounds)
    .filter((bound) =>
      !Number.isNaN(bounds[bound])
      && bounds[bound] !== 9999
      && bounds[bound] !== -9999);
  if (changedBounds.length !== 4) {
    return Maybe.nothing();
  }

  return Maybe.just([
    [bounds.minLat as number, bounds.minLong as number] as Leaflet.LatLngTuple,
    [bounds.maxLat as number, bounds.maxLong as number] as Leaflet.LatLngTuple,
  ]);
};

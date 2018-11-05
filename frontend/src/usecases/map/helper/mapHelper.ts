import * as Leaflet from 'leaflet';
import {firstUpperTranslated} from '../../../services/translationService';
import {Dictionary, Status, statusFor} from '../../../types/Types';
import {Bounds, IdentifiablePosition, MapMarker, MapMarkerApiResponse} from '../mapModels';

export const flattenMapMarkers = (markers: Dictionary<MapMarker>): MapMarker[] =>
  markers
    ? Object.keys(markers).map((key: string) => markers[key])
    : [];

export const flatMapMarkers = (response: MapMarkerApiResponse): MapMarker[] => {
  const mapMarkers: MapMarker[] = [];
  Object.keys(response.markers)
    .forEach((status: Status) =>
      response.markers[status].forEach((position: IdentifiablePosition) =>
        mapMarkers.push(({...position, status: statusFor(status.toLowerCase())}))),
    );
  return mapMarkers;
};

interface MarkerBounds {
  minLat: number;
  maxLat: number;
  minLong: number;
  maxLong: number;
}

export const boundsFromMarkers = (markers: Dictionary<MapMarker>): Bounds => {
  const mapMarkers: MapMarker[] = flattenMapMarkers(markers);

  const bounds: MarkerBounds = Object.keys(mapMarkers)
    .reduce(
      (sum: MarkerBounds, markerId: string) => {
        const {latitude, longitude} = mapMarkers[markerId];

        if (!isNaN(latitude)) {
          if (latitude < sum.minLat) {
            sum.minLat = latitude;
          }
          if (latitude > sum.maxLat) {
            sum.maxLat = latitude;
          }
        }

        if (!isNaN(longitude)) {
          if (longitude < sum.minLong) {
            sum.minLong = longitude;
          }
          if (longitude > sum.maxLong) {
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

  const changedBounds: string[] = Object.keys(bounds)
    .filter((bound) =>
      !Number.isNaN(bounds[bound])
      && bounds[bound] !== 9999
      && bounds[bound] !== -9999);

  if (changedBounds.length !== 4) {
    return undefined;
  } else {
    return [
      [bounds.minLat as number, bounds.minLong as number] as Leaflet.LatLngTuple,
      [bounds.maxLat as number, bounds.maxLong as number] as Leaflet.LatLngTuple,
    ];
  }
};

const lowConfidenceTextInfo = (
  totalMeters: number,
  totalMarkers: number,
  translateWith: (count: number) => string,
): string | undefined => {
  const numMarkersWithLowConfidence = totalMeters - totalMarkers;
  return numMarkersWithLowConfidence ? translateWith(numMarkersWithLowConfidence) : undefined;
};

export const meterLowConfidenceTextInfo = (
  query: string | undefined,
  totalMeters: number,
  totalMarkers: number,
): string | undefined =>
  lowConfidenceTextInfo(
    totalMeters,
    totalMarkers,
    (count: number) => query
      ? firstUpperTranslated(
        '{{count}} meter of your selection, are not displayed in the map',
        {count},
      )
      : firstUpperTranslated(
        '{{count}} meter are not displayed in the map due to low accuracy',
        {count},
      ),
  );

export const gatewayLowConfidenceTextInfo = (
  query: string | undefined,
  totalMeters: number,
  totalMarkers: number,
): string | undefined =>
  lowConfidenceTextInfo(
    totalMeters,
    totalMarkers,
    (count: number) => query
      ? firstUpperTranslated(
        '{{count}} gateway of your selection, are not displayed in the map', {count},
      )
      : firstUpperTranslated(
        '{{count}} gateway are not displayed in the map due to low accuracy', {count},
      ),
  );

export const maxZoom = 18;

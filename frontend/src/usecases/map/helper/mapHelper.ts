import * as Leaflet from 'leaflet';
import {firstUpperTranslated} from '../../../services/translationService';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {Dictionary} from '../../../types/Types';
import {MapMarker} from '../mapModels';

export const metersWithinThreshold = (markers: Dictionary<MapMarker>): MapMarker[] =>
  markers
    ? Object.keys(markers)
      .map((key: string) => markers[key])
      .filter(isGeoPositionWithinThreshold)
    : [];

export const isGeoPositionWithinThreshold =
  ({latitude, longitude, confidence}: MapMarker) =>
    latitude !== undefined && longitude !== undefined && confidence >= 0.75;

interface MarkerBounds {
  minLat: number;
  maxLat: number;
  minLong: number;
  maxLong: number;
}

export const boundsFromMarkers = (markers: Dictionary<MapMarker>): Leaflet.LatLngTuple[] | undefined => {
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
    return undefined;
  } else {
    return [
      [bounds.minLat as number, bounds.minLong as number] as Leaflet.LatLngTuple,
      [bounds.maxLat as number, bounds.maxLong as number] as Leaflet.LatLngTuple,
    ];
  }
};

const lowConfidenceTextInfo = ({result, entities}: DomainModel<MapMarker>, text: string): string | undefined => {
  const numMarkersWithLowConfidence = result.length - metersWithinThreshold(entities).length;

  return numMarkersWithLowConfidence
    ? firstUpperTranslated(text, {count: numMarkersWithLowConfidence})
    : undefined;
};

export const meterLowConfidenceTextInfo = (meterMapMarkers: DomainModel<MapMarker>): string | undefined =>
  lowConfidenceTextInfo(meterMapMarkers, '{{count}} meters are not displayed in the map due to low accuracy');

export const gatewayLowConfidenceTextInfo = (mapMarkers: DomainModel<MapMarker>): string | undefined =>
  lowConfidenceTextInfo(mapMarkers, '{{count}} gateways are not displayed in the map due to low accuracy');

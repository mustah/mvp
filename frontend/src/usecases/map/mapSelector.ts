import {createSelector} from 'reselect';
import {MapMarker} from './mapModels';
import {IdNamed} from '../../types/Types';
import * as L from 'leaflet';
import {ClusterContainerProps} from './containers/ClusterContainer';

const getMarkers = (props: ClusterContainerProps): {[key: string]: MapMarker} | MapMarker => {
  return props.markers;
} ;

export const getExtendedMarkers = createSelector([getMarkers], (markers: {[key: string]: MapMarker} | MapMarker) => {
    let tmpMarkers: { [key: string]: MapMarker } = {};
    if (isMapMarker(markers)) {
      tmpMarkers[0] = markers;
    } else {
      tmpMarkers = markers;
    }

    const confidenceThreshold: number = 0.7;
    // TODO type array
    const leafletMarkers: any[] = [];

    if (tmpMarkers) {
      Object.keys(tmpMarkers).forEach((key: string) => {
        const marker = tmpMarkers[key];
        const {latitude, longitude, confidence} = marker.position;

        if (latitude && longitude && confidence >= confidenceThreshold) {
          leafletMarkers.push({
            lat: latitude,
            lng: longitude,
            options: {
              icon: L.icon({
                iconUrl: getStatusIcon(marker.status),
              }),
              mapMarker: marker,
            },
          });
        }
      });
    }

    return leafletMarkers;
  },
);

const getStatusIcon = ({id}: IdNamed): string => icons[id] || 'assets/images/marker-icon.png';

const isMapMarker = (obj: any): obj is MapMarker => {
  return obj && obj.status !== undefined && obj.position !== undefined;
};

const icons = {
  0: 'assets/images/marker-icon-ok.png',
  1: 'assets/images/marker-icon-ok.png',
  2: 'assets/images/marker-icon-warning.png',
  3: 'assets/images/marker-icon-error.png',
};

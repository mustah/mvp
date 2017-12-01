import * as L from 'leaflet';
import * as React from 'react';
import MarkerClusterGroup from 'react-leaflet-markercluster';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {IdNamed} from '../../../types/Types';
import {openClusterDialog} from '../mapActions';
import {ExtendedMarker, MapMarker} from '../mapModels';

interface DispatchToProps {
  openClusterDialog?: (marker: ExtendedMarker) => void;
}

interface OwnProps {
  markers: {[key: string]: MapMarker} | MapMarker;
}

class Cluster extends React.Component<DispatchToProps & OwnProps> {
  render() {
    const {
      openClusterDialog,
      markers,
    } = this.props;

    let tmpMarkers: {[key: string]: MapMarker} = {};
    if (isMapMarker(markers)) {
      tmpMarkers[0] = markers;
    } else {
      tmpMarkers = markers;
    }

    const confidenceThreshold: number = 0.7;
    // TODO type array
    const leafletMarkers: any[] = [];

    // TODO break up marker icon logic into methods and add tests
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

    const markerClusterOptions = {
      iconCreateFunction: handleIconCreate,
      chunkedLoading: true,
      showCoverageOnHover: true,
      maxClusterRadius: getZoomBasedRadius,
    };

    const renderCluster = () => leafletMarkers.length > 0 && (
      <MarkerClusterGroup
        markers={leafletMarkers}
        onMarkerClick={openClusterDialog}
        options={markerClusterOptions}
      />);

    return (
      renderCluster()
    );
  }
}

const getZoomBasedRadius = (zoom: number) => {
  if (zoom < maxZoom) {
    return 80;
  } else {
    return 5;
  }
};

const handleIconCreate = (cluster: MarkerClusterGroup) => {
  const x = getClusterDimensions(cluster.getChildCount());

  return L.divIcon({
    html: `<span>${cluster.getChildCount()}</span>`,
    className: getClusterCssClass(cluster),
    iconSize: L.point(x, x, true),
  });
};

// TODO needs to be shared with Map
const maxZoom = 18;

const getClusterCssClass = (cluster: MarkerClusterGroup): string => {
  // TODO Test performance!
  // TODO Find status of the marker instead of guessing by checking iconUrl
  // Set cluster css class depending on underlying marker icons

  let errorCount = 0;
  let warningCount = 0;

  cluster.getAllChildMarkers().forEach((child: L.Marker) => {
    if (child.options.icon) {
      if (child.options.icon.options.iconUrl === 'assets/images/marker-icon-error.png') {
        errorCount++;
      } else if (child.options.icon.options.iconUrl === 'assets/images/marker-icon-warning.png') {
        warningCount++;
      }
    }
  });

  let percent = (cluster.getChildCount() - errorCount - warningCount) / cluster.getChildCount() * 100;
  percent = Math.floor(percent);

  let cssClass: string;
  if (percent === 100) {
    cssClass = 'marker-cluster-ok';
  } else if (percent > 90) {
    cssClass = 'marker-cluster-warning';
  } else {
    cssClass = 'marker-cluster-error';
  }

  return cssClass;
};

const icons = {
  0: 'assets/images/marker-icon-ok.png',
  1: 'assets/images/marker-icon-ok.png',
  2: 'assets/images/marker-icon-warning.png',
  3: 'assets/images/marker-icon-error.png',
};

const getStatusIcon = ({id}: IdNamed): string => icons[id] || 'assets/images/marker-icon.png';

const getClusterDimensions = (clusterCount: number): number => {
  let x = clusterCount / 9;

  if (x > 90) {
    x = 100;
  } else if (x < 30) {
    x = 30;
  }

  return x;
};

const isMapMarker = (obj: any): obj is MapMarker => {
  return obj && obj.status !== undefined && obj.position !== undefined;
};

const mapDispatchToProps = dispatch => bindActionCreators({
  openClusterDialog,
}, dispatch);

export const ClusterContainer = connect<{}, DispatchToProps, OwnProps>(null, mapDispatchToProps)(Cluster);

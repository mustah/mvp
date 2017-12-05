import * as L from 'leaflet';
import * as React from 'react';
import MarkerClusterGroup from 'react-leaflet-markercluster';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {openClusterDialog} from '../mapActions';
import {ExtendedMarker, MapMarker} from '../mapModels';
import {getExtendedMarkers} from '../mapSelector';

interface DispatchToProps {
  openClusterDialog?: (marker: ExtendedMarker) => void;
}

export interface ClusterContainerProps {
  markers: {[key: string]: MapMarker} | MapMarker;
}

class Cluster extends React.Component<DispatchToProps & ClusterContainerProps> {
  render() {
    const {
      openClusterDialog,
    } = this.props;

    const leafletMarkers: any[] = getExtendedMarkers(this.props);

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

const getClusterDimensions = (clusterCount: number): number => {
  let x = clusterCount / 9;

  if (x > 90) {
    x = 100;
  } else if (x < 30) {
    x = 30;
  }

  return x;
};

const mapDispatchToProps = dispatch => bindActionCreators({
  openClusterDialog,
}, dispatch);

export const ClusterContainer = connect<{}, DispatchToProps, ClusterContainerProps>(null, mapDispatchToProps)(Cluster);

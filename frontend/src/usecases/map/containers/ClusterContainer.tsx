import * as Leaflet from 'leaflet';
import * as React from 'react';
import MarkerClusterGroup from 'react-leaflet-markercluster';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {imagePathFor} from '../../../app/routes';
import {Dictionary} from '../../../types/Types';
import {makeLeafletCompatibleMarkersFrom} from '../helper/clusterHelper';
import {maxZoom} from '../helper/mapHelper';
import {openClusterDialog} from '../mapActions';
import {MapMarker, Marker} from '../mapModels';

const getZoomBasedRadius = (zoom: number) => {
  if (zoom < maxZoom) {
    return 80;
  } else {
    return 5;
  }
};

const handleIconCreate = (cluster: MarkerClusterGroup): Leaflet.DivIcon => {
  const x = getClusterDimensions(cluster.getChildCount());

  return Leaflet.divIcon({
    html: `<span>${cluster.getChildCount()}</span>`,
    className: getClusterCssClass(cluster),
    iconSize: Leaflet.point(x, x, true),
  });
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

const getClusterCssClass = (cluster: MarkerClusterGroup): string => {
  // TODO Test performance!
  // TODO Find status of the marker instead of guessing by checking iconUrl
  // Set cluster css class depending on underlying marker icons

  let errorCount = 0;
  let warningCount = 0;

  cluster.getAllChildMarkers().forEach(({options: {icon}}: Leaflet.Marker) => {
    if (icon) {
      if (icon.options.iconUrl === imagePathFor('marker-icon-error.png')) {
        errorCount++;
      } else if (icon.options.iconUrl === imagePathFor('marker-icon-warning.png')) {
        warningCount++;
      }
    }
  });

  let percent = (cluster.getChildCount() - errorCount - warningCount) / cluster.getChildCount() * 100;
  percent = Math.floor(percent);

  let cssClass: string;
  if (percent === 100) {
    cssClass = 'marker-cluster-ok';
  } else if (percent > 70) {
    cssClass = 'marker-cluster-warning';
  } else {
    cssClass = 'marker-cluster-error';
  }

  return cssClass;
};

interface DispatchToProps {
  openClusterDialog: (marker: Marker) => void;
}

interface OwnProps {
  markers: Dictionary<MapMarker> | MapMarker;
}

const Cluster = ({openClusterDialog, markers}: DispatchToProps & OwnProps) => {
  const leafletMarkers: Marker[] = makeLeafletCompatibleMarkersFrom(markers);

  const markerClusterOptions = {
    iconCreateFunction: handleIconCreate,
    chunkedLoading: true,
    showCoverageOnHover: true,
    maxClusterRadius: getZoomBasedRadius,
  };

  return leafletMarkers.length > 0 ? (
    <MarkerClusterGroup
      markers={leafletMarkers}
      onMarkerClick={openClusterDialog}
      options={markerClusterOptions}
    />) : null;
};

const mapDispatchToProps = (dispatch) => bindActionCreators({
  openClusterDialog,
}, dispatch);

export const ClusterContainer =
  connect<{}, DispatchToProps, OwnProps>(null, mapDispatchToProps)(Cluster);

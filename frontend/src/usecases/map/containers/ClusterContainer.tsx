import * as Leaflet from 'leaflet';
import * as React from 'react';
import MarkerClusterGroup from 'react-leaflet-markercluster';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {IdNamed} from '../../../types/Types';
import {openClusterDialog} from '../mapActions';
import {MapMarker, MapMarkerItem, Marker} from '../mapModels';

const icons = {
  0: 'assets/images/marker-icon-ok.png',
  1: 'assets/images/marker-icon-ok.png',
  2: 'assets/images/marker-icon-warning.png',
  3: 'assets/images/marker-icon-error.png',
};

const getStatusIcon = ({id}: IdNamed): string => icons[id] || 'assets/images/marker-icon.png';

const isWithinThreshold = ({position: {latitude, longitude, confidence}}: MapMarker) =>
  latitude && longitude && confidence >= 0.7;

const makeMarker = (marker: MapMarker): Marker => ({
  position: [marker.position.latitude, marker.position.longitude] as Leaflet.LatLngTuple,
  options: {
    icon: Leaflet.icon({iconUrl: getStatusIcon(marker.status)}),
    mapMarkerItem: marker as MapMarkerItem,
  },
});

const makeLeafletMarkerFrom = (markers: DomainModel<MapMarker>): Marker[] => {
  return Object.keys(markers)
    .map((key: string) => markers[key])
    .filter(isWithinThreshold)
    .map(makeMarker);
};

interface DispatchToProps {
  openClusterDialog: (marker: Marker) => void;
}

interface OwnProps {
  markers: DomainModel<MapMarker>;
}

const Cluster = ({openClusterDialog, markers}: DispatchToProps & OwnProps) => {
  const leafletMarkers: Marker[] = makeLeafletMarkerFrom(markers);

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

// TODO needs to be shared with Map
const maxZoom = 18;

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

const getClusterCssClass = (cluster: MarkerClusterGroup): string => {
  // TODO Test performance!
  // TODO Find status of the marker instead of guessing by checking iconUrl
  // Set cluster css class depending on underlying marker icons

  let errorCount = 0;
  let warningCount = 0;

  cluster.getAllChildMarkers().forEach(({options: {icon}}: Leaflet.Marker) => {
    if (icon) {
      if (icon.options.iconUrl === 'assets/images/marker-icon-error.png') {
        errorCount++;
      } else if (icon.options.iconUrl === 'assets/images/marker-icon-warning.png') {
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

export const ClusterContainer = connect<{}, DispatchToProps, OwnProps>(null, mapDispatchToProps)(Cluster);

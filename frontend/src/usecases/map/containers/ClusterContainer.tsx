import * as Leaflet from 'leaflet';
import * as React from 'react';
import MarkerClusterGroup from 'react-leaflet-markercluster';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {isDefined} from '../../../helpers/commonHelpers';
import {Dictionary} from '../../../types/Types';
import {
  isAlarmIconUrl,
  isErrorIconUrl,
  isWarningIconUrl,
  makeLeafletCompatibleMarkersFrom,
} from '../helper/clusterHelper';
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
  const faults = cluster.getAllChildMarkers()
    .map(({options: {icon}}: Leaflet.Marker) => icon)
    .filter(isDefined)
    .filter((icon) => isErrorIconUrl(icon.options.iconUrl)
                      || isAlarmIconUrl(icon.options.iconUrl)
                      || isWarningIconUrl(icon.options.iconUrl))
    .length;

  const numChildren = cluster.getChildCount();
  const className = numChildren === faults ? 'error' : faults ? 'warning' : 'ok';

  return `marker-cluster ${className}`;
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

  return leafletMarkers.length > 0
    ? (
      <MarkerClusterGroup
        markers={leafletMarkers}
        onMarkerClick={openClusterDialog}
        options={markerClusterOptions}
      />
    )
    : null;
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  openClusterDialog,
}, dispatch);

export const ClusterContainer =
  connect<{}, DispatchToProps, OwnProps>(null, mapDispatchToProps)(Cluster);

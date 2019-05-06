import * as Leaflet from 'leaflet';
import * as React from 'react';
import MarkerClusterGroup from 'react-leaflet-markercluster';
import {history, routes} from '../../../app/routes';
import {isDefined} from '../../../helpers/commonHelpers';
import {
  isAlarmIconUrl,
  isErrorIconUrl,
  isWarningIconUrl,
  makeLeafletCompatibleMarkersFrom,
} from '../helper/clusterHelper';
import {maxZoom} from '../helper/mapHelper';
import {MapMarkers, Marker} from '../mapModels';

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

export const MarkerCluster = ({mapMarkers}: MapMarkers) => {
  const leafletMarkers: Marker[] = makeLeafletCompatibleMarkersFrom(mapMarkers || {});

  const markerClusterOptions = {
    iconCreateFunction: handleIconCreate,
    chunkedLoading: true,
    showCoverageOnHover: true,
    maxClusterRadius: getZoomBasedRadius,
  };

  const toMeterDetails = ({options: {mapMarkerItem}}: Marker) =>
    history.push(`${routes.meter}/${mapMarkerItem}`);

  return leafletMarkers.length > 0
    ? (
      <MarkerClusterGroup
        markers={leafletMarkers}
        onMarkerClick={toMeterDetails}
        options={markerClusterOptions}
      />
    )
    : null;
};

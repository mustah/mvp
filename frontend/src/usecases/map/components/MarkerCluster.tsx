import * as Leaflet from 'leaflet';
import * as React from 'react';
import {Marker as LeafletMarker} from 'react-leaflet';
import MarkerClusterGroup from 'react-leaflet-markercluster';
import {history, routes} from '../../../app/routes';
import {Callback} from '../../../types/Types';
import {maxZoom} from '../helper/mapHelper';
import {MapMarkersProps, Marker} from '../mapModels';

const getZoomBasedRadius = (zoom: number) => {
  if (zoom < maxZoom) {
    return 80;
  } else {
    return 5;
  }
};

const iconCreateFunctionHandler = (faults: number) =>
  (cluster: MarkerClusterGroup): Leaflet.DivIcon => {
    const childCount: number = cluster.getChildCount();
    const x = getClusterDimensions(childCount);

    return Leaflet.divIcon({
      html: `<span>${childCount}</span>`,
      className: `marker-cluster ${childCount === faults ? 'error' : faults ? 'warning' : 'ok'}`,
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

interface WithLeafletElement {
  leafletElement: {
    clearLayers: Callback;
  };
}

export const MarkerCluster = ({mapMarkerClusters: {markers, faults}}: MapMarkersProps) => {
  const leafletMarkers = React.useMemo(() =>
    markers.map(({position, options: {icon, mapMarkerItem}}: Marker) =>
      <LeafletMarker meterId={mapMarkerItem} position={position} key={'marker-' + mapMarkerItem} icon={icon}/>
    ), [markers]);

  const markerClusterGroupElement = React.useRef<WithLeafletElement>(null);

  const onClickMeter = ({type, layer: {options: {meterId}}}) => {
    const current = markerClusterGroupElement.current;
    if (type === 'click' && current !== null) {
      current.leafletElement.clearLayers();
      history.push(`${routes.meter}/${meterId}`);
    }
  };

  return (
    <MarkerClusterGroup
      chunkedLoading={true}
      iconCreateFunction={iconCreateFunctionHandler(faults)}
      maxClusterRadius={getZoomBasedRadius}
      showCoverageOnHover={true}
      onclick={onClickMeter}
      ref={markerClusterGroupElement}
    >
      {leafletMarkers}
    </MarkerClusterGroup>
  );
};

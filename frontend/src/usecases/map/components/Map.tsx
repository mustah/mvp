import {default as Leaflet, FitBoundsOptions, LatLngBoundsLiteral, LatLngTuple, LeafletMouseEvent} from 'leaflet';
import 'leaflet.markercluster/dist/MarkerCluster.css';
import 'leaflet.markercluster/dist/MarkerCluster.Default.css';
import 'leaflet/dist/leaflet.css';
import * as React from 'react';
import {Map as LeafletMap, TileLayer} from 'react-leaflet';
import {borderRadius} from '../../../app/themes';
import {Column} from '../../../components/layouts/column/Column';
import {useResizeWindow} from '../../../hooks/resizeWindowHook';
import {useFallbackTilesUrl} from '../helper/fallbackTilesUrlHook';
import {maxZoom} from '../helper/mapHelper';
import {MapComponentProps, MapMarkersProps, MapProps, OnCenterMapEvent} from '../mapModels';
import {defaultZoomLevel} from '../mapReducer';
import {LowConfidenceInfo} from './LowConfidenceInfo';
import './Map.scss';
import {MarkerCluster} from './MarkerCluster';

const defaultCenter: LatLngTuple = [62.3919741, 15.0685715];
const defaultBoundOptions: FitBoundsOptions = {padding: [100, 100]};

const toggleScrollWheelZoom = ({target}: LeafletMouseEvent): void => {
  if (target.scrollWheelZoom.enabled()) {
    target.scrollWheelZoom.disable();
  } else {
    target.scrollWheelZoom.enable();
  }
};

type Props = MapComponentProps & OnCenterMapEvent;

const Map = ({
  bounds,
  center,
  children,
  height,
  id,
  lowConfidenceText,
  onCenterMap,
  width,
  zoom = defaultZoomLevel,
}: Props) => {
  const {tilesUrl, updateTilesUrl} = useFallbackTilesUrl();
  const style: React.CSSProperties = {
    height,
    width,
    borderRadius,
    borderTopLeftRadius: 0,
    borderTopRightRadius: 0
  };

  let mapCenter: LatLngTuple | undefined;
  let mapBound: LatLngBoundsLiteral | undefined;
  let mapBoundOptions: Leaflet.FitBoundsOptions | undefined;
  if (center) {
    mapCenter = [center.latitude, center.longitude];
  } else if (bounds) {
    mapBound = bounds;
    mapBoundOptions = defaultBoundOptions;
  } else {
    mapCenter = defaultCenter;
  }

  const onCenterMapHandler = ({target: {_lastCenter: {lat, lng}, _zoom: zoom}}) =>
    onCenterMap({id, zoom, center: {latitude: lat, longitude: lng}});

  return (
    <Column>
      <LeafletMap
        bounds={mapBound}
        boundsOptions={mapBoundOptions}
        center={mapCenter}
        className="Map"
        maxZoom={maxZoom}
        minZoom={0}
        scrollWheelZoom={false}
        onclick={toggleScrollWheelZoom}
        style={style}
        onzoomend={onCenterMapHandler}
        ondragend={onCenterMapHandler}
        zoom={zoom}
      >
        <LowConfidenceInfo>{lowConfidenceText}</LowConfidenceInfo>
        <TileLayer url={tilesUrl} ontileerror={updateTilesUrl}/>
        {children}
      </LeafletMap>
    </Column>
  );
};

const minHeight = 499;

export const ResponsiveMap = (props: Props) => {
  const {height: innerHeight} = useResizeWindow();
  const paddingBottom = props.paddingBottom || minHeight;
  const height = innerHeight - paddingBottom;
  return <Map height={height <= minHeight ? minHeight : height} {...props}/>;
};

export const MapMarkerCluster = ({mapMarkerClusters, ...mapProps}: Props & MapMarkersProps) => (
  <Map {...mapProps}>
    <MarkerCluster mapMarkerClusters={mapMarkerClusters}/>
  </Map>
);

export const ResponsiveMapMarkerClusters = ({mapMarkerClusters, ...mapProps}: MapProps) => (
  <ResponsiveMap {...mapProps} paddingBottom={197}>
    <MarkerCluster mapMarkerClusters={mapMarkerClusters}/>
  </ResponsiveMap>
);

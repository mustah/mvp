import {FitBoundsOptions, LatLngTuple, LeafletMouseEvent} from 'leaflet';
import 'leaflet.markercluster/dist/MarkerCluster.css';
import 'leaflet.markercluster/dist/MarkerCluster.Default.css';
import 'leaflet/dist/leaflet.css';
import * as React from 'react';
import {Map as LeafletMap, MapProps as LeafletMapProps, TileLayer} from 'react-leaflet';
import {borderRadius} from '../../../app/themes';
import {Column} from '../../../components/layouts/column/Column';
import {useResizeWindow} from '../../../hooks/resizeWindowHook';
import {useFallbackTilesUrl} from '../helper/fallbackTilesUrlHook';
import {maxZoom} from '../helper/mapHelper';
import {MapComponentProps, MapMarkers, MapProps, OnCenterMapEvent} from '../mapModels';
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

  const mapProps: LeafletMapProps = {zoom};
  if (center) {
    mapProps.center = [center.latitude, center.longitude];
  } else if (bounds) {
    mapProps.bounds = bounds;
    mapProps.boundsOptions = defaultBoundOptions;
  } else {
    mapProps.center = defaultCenter;
  }

  const onCenterMapHandler = ({target: {_lastCenter: {lat, lng}, _zoom: zoom}}) =>
    onCenterMap({id, zoom, center: {latitude: lat, longitude: lng}});

  return (
    <Column>
      <LeafletMap
        maxZoom={maxZoom}
        minZoom={0}
        className="Map"
        scrollWheelZoom={false}
        onclick={toggleScrollWheelZoom}
        style={style}
        onzoomend={onCenterMapHandler}
        ondragend={onCenterMapHandler}
        {...mapProps}
      >
        <LowConfidenceInfo>{lowConfidenceText}</LowConfidenceInfo>
        <TileLayer url={tilesUrl} ontileerror={updateTilesUrl}/>
        {children}
      </LeafletMap>
    </Column>
  );
};

const minHeight = 504;

export const ResponsiveMap = (props: Props) => {
  const {height: innerHeight} = useResizeWindow();
  const paddingBottom = props.paddingBottom || minHeight;
  const height = innerHeight - paddingBottom;
  return <Map height={height <= minHeight ? minHeight : height} {...props}/>;
};

export const MapMarkerCluster = ({mapMarkers, ...mapProps}: Props & MapMarkers) => (
  <Map {...mapProps}>
    <MarkerCluster mapMarkers={mapMarkers}/>
  </Map>
);

export const ResponsiveMapMarkerClusters = ({mapMarkers, ...mapProps}: MapProps) => (
  <ResponsiveMap {...mapProps} paddingBottom={270}>
    <MarkerCluster mapMarkers={mapMarkers}/>
  </ResponsiveMap>
);

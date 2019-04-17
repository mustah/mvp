import {FitBoundsOptions, LatLngTuple, LeafletMouseEvent} from 'leaflet';
import 'leaflet.markercluster/dist/MarkerCluster.css';
import 'leaflet.markercluster/dist/MarkerCluster.Default.css';
import 'leaflet/dist/leaflet.css';
import * as React from 'react';
import {Map as LeafletMap, MapProps, TileLayer} from 'react-leaflet';
import {borderRadius} from '../../../app/themes';
import {Column} from '../../../components/layouts/column/Column';
import {useResizeWindow} from '../../../hooks/resizeWindowHook';
import {GeoPosition} from '../../../state/domain-models/location/locationModels';
import {WithChildren} from '../../../types/Types';
import {useFallbackTilesUrl} from '../helper/fallbackTilesUrlHook';
import {maxZoom} from '../helper/mapHelper';
import {Bounds} from '../mapModels';
import {LowConfidenceInfo} from './LowConfidenceInfo';
import './Map.scss';

interface Props extends WithChildren {
  bounds?: Bounds;
  height?: number;
  width?: number;
  paddingBottom?: number;
  lowConfidenceText?: string;
  viewCenter?: GeoPosition;
  key?: string;
}

const toggleScrollWheelZoom = ({target}: LeafletMouseEvent): void => {
  if (target.scrollWheelZoom.enabled()) {
    target.scrollWheelZoom.disable();
  } else {
    target.scrollWheelZoom.enable();
  }
};

const defaultCenter: LatLngTuple = [62.3919741, 15.0685715];
const defaultBoundOptions: FitBoundsOptions = {padding: [100, 100]};

const minHeight = 504;

export const ResponsiveMap = (props: Props) => {
  const {height: innerHeight} = useResizeWindow();
  const paddingBottom = props.paddingBottom || minHeight;
  const height = innerHeight - paddingBottom;
  return <Map height={height <= minHeight ? minHeight : height} {...props}/>;
};

export const Map = ({
  height,
  width,
  viewCenter,
  children,
  lowConfidenceText,
  bounds,
}: Props) => {
  const {tilesUrl, updateTilesUrl} = useFallbackTilesUrl();
  const style: React.CSSProperties = {
    height,
    width,
    borderRadius,
    borderTopLeftRadius: 0,
    borderTopRightRadius: 0
  };
  const centerProps: MapProps = {zoom: 7};

  if (viewCenter) {
    centerProps.center = [viewCenter.latitude, viewCenter.longitude];
    centerProps.zoom = 17;
  } else if (bounds) {
    centerProps.bounds = bounds;
    centerProps.boundsOptions = defaultBoundOptions;
  } else {
    centerProps.center = defaultCenter;
  }

  return (
    <Column>
      <LeafletMap
        maxZoom={maxZoom}
        minZoom={0}
        className="Map"
        scrollWheelZoom={false}
        onclick={toggleScrollWheelZoom}
        style={style}
        {...centerProps}
      >
        <LowConfidenceInfo>{lowConfidenceText}</LowConfidenceInfo>
        <TileLayer url={tilesUrl} ontileerror={updateTilesUrl}/>
        {children}
      </LeafletMap>
    </Column>
  );
};

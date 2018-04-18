import {LatLngTuple} from 'leaflet';
import 'leaflet.markercluster/dist/MarkerCluster.css';
import 'leaflet.markercluster/dist/MarkerCluster.Default.css';
import 'leaflet/dist/leaflet.css';
import * as React from 'react';
import {Map as LeafletMap, MapProps, TileLayer} from 'react-leaflet';
import Control from 'react-leaflet-control';
import * as Leaflet from '../../../../node_modules/@types/react-leaflet/node_modules/@types/leaflet';
import {Column} from '../../../components/layouts/column/Column';
import {GeoPosition} from '../../../state/domain-models/location/locationModels';
import {boundsFromMarkers} from '../helper/mapHelper';
import './Map.scss';

interface Props {
  height?: number;
  width?: number;
  defaultZoom?: number;
  viewCenter?: GeoPosition;
  children?: React.ReactElement<any>;
  lowConfidenceText?: string;
  boundOptions?: Leaflet.FitBoundsOptions;
}

const toggleScrollWheelZoom = ({target}: Leaflet.LeafletMouseEvent): void => {
  if (target.scrollWheelZoom.enabled()) {
    target.scrollWheelZoom.disable();
  } else {
    target.scrollWheelZoom.enable();
  }
};

const defaultCenter: LatLngTuple = [62.3919741, 15.0685715];

export const Map = ({
  height,
  width,
  defaultZoom = 7,
  viewCenter,
  children,
  lowConfidenceText,
  boundOptions = {padding: [100, 100]},
}: Props) => {

  const style = {height, width};

  const centerProps: MapProps = {};

  if (viewCenter) {
    centerProps.center = [viewCenter.latitude, viewCenter.longitude];
  } else if (children && children.props && children.props.markers) {
    const bounds = boundsFromMarkers(children.props.markers);
    if (bounds) {
      centerProps.bounds = bounds;
      centerProps.boundsOptions = boundOptions;
    } else {
      centerProps.center = defaultCenter;
    }
  } else {
    centerProps.center = defaultCenter;
  }

  const renderMarkersWithLowConfidenceInfoText = lowConfidenceText
    ? (
      <Control position="topright" className="LowConfidence">
        <p>{lowConfidenceText}</p>
      </Control>
    )
    : null;

  return (
    <Column>
      <LeafletMap
        maxZoom={18}
        minZoom={0}
        zoom={defaultZoom}
        className="Map"
        scrollWheelZoom={false}
        onclick={toggleScrollWheelZoom}
        style={style}
        {...centerProps}
      >
        {renderMarkersWithLowConfidenceInfoText}
        <TileLayer url="https://{s}.tile.openstreetmap.se/hydda/full/{z}/{x}/{y}.png"/>
        {children}
      </LeafletMap>
    </Column>
  );
};

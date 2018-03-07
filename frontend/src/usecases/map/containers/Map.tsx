import 'leaflet.markercluster/dist/MarkerCluster.css';
import 'leaflet.markercluster/dist/MarkerCluster.Default.css';
import 'leaflet/dist/leaflet.css';
import {LatLngTuple} from 'leaflet';
import * as React from 'react';
import {Map as LeafletMap, TileLayer} from 'react-leaflet';
import * as Leaflet from '../../../../node_modules/@types/react-leaflet/node_modules/@types/leaflet';
import {Column} from '../../../components/layouts/column/Column';
import {GeoPosition} from '../../../state/domain-models/location/locationModels';
import {Children} from '../../../types/Types';
import './Map.scss';

interface Props {
  height?: number;
  width?: number;
  defaultZoom?: number;
  viewCenter?: GeoPosition;
  children?: Children;
}

const defaultViewCenter: GeoPosition = {latitude: 56.142226, longitude: 13.402965, confidence: 1};

const toggleScrollWheelZoom = ({target}: Leaflet.LeafletMouseEvent): void => {
  if (target.scrollWheelZoom.enabled()) {
    target.scrollWheelZoom.disable();
  } else {
    target.scrollWheelZoom.enable();
  }
};

export const Map = (props: Props) => {
  const {
    height,
    width,
    defaultZoom = 7,
    viewCenter = defaultViewCenter,
    children,
  } = props;

  const style = {height, width};
  const center: LatLngTuple = [viewCenter.latitude, viewCenter.longitude];

  return (
    <Column>
      <LeafletMap
        center={center}
        maxZoom={18}
        minZoom={3}
        zoom={defaultZoom}
        className="Map"
        scrollWheelZoom={false}
        onclick={toggleScrollWheelZoom}
        style={style}
      >
        <TileLayer url="https://{s}.tile.openstreetmap.se/hydda/full/{z}/{x}/{y}.png"/>
        {children}
      </LeafletMap>
    </Column>
  );
};

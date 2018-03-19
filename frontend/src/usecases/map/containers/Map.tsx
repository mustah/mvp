import {LatLngTuple} from 'leaflet';
import 'leaflet.markercluster/dist/MarkerCluster.css';
import 'leaflet.markercluster/dist/MarkerCluster.Default.css';
import 'leaflet/dist/leaflet.css';
import * as React from 'react';
import {Map as LeafletMap, MapProps, TileLayer} from 'react-leaflet';
import MarkerClusterGroup from 'react-leaflet-markercluster';
import * as Leaflet from '../../../../node_modules/@types/react-leaflet/node_modules/@types/leaflet';
import {Column} from '../../../components/layouts/column/Column';
import {Maybe} from '../../../helpers/Maybe';
import {GeoPosition} from '../../../state/domain-models/location/locationModels';
import './Map.scss';

interface Props {
  height?: number;
  width?: number;
  defaultZoom?: number;
  viewCenter?: GeoPosition;
  children?: React.ReactElement<any>;
}

const toggleScrollWheelZoom = ({target}: Leaflet.LeafletMouseEvent): void => {
  if (target.scrollWheelZoom.enabled()) {
    target.scrollWheelZoom.disable();
  } else {
    target.scrollWheelZoom.enable();
  }
};

const boundsFromMarkers = (markers: MarkerClusterGroup): Maybe<LatLngTuple[]> => {
  const bounds = Object.keys(markers).reduce(
    (sum: any, markerId: string) => {
      const {latitude, longitude} = markers[markerId].location.position;

      if (!isNaN(parseFloat(latitude))) {
        if (latitude < sum.minLat) {
          sum.minLat = latitude;
        } else if (latitude > sum.maxLat) {
          sum.maxLat = latitude;
        }
      }

      if (!isNaN(parseFloat(longitude))) {
        if (longitude < sum.minLong) {
          sum.minLong = longitude;
        } else if (longitude > sum.maxLong) {
          sum.maxLong = longitude;
        }
      }

      return sum;
    }, {
      minLat: 9999,
      maxLat: -9999,
      minLong: 9999,
      maxLong: -9999,
    },
  );

  const changedBounds = Object.keys(bounds)
    .filter((bound) =>
      !Number.isNaN(bounds[bound])
      && bounds[bound] !== 9999
      && bounds[bound] !== -9999);
  if (changedBounds.length !== 4) {
    return Maybe.nothing();
  }

  return Maybe.just([
    [bounds.minLat as number, bounds.minLong as number] as LatLngTuple,
    [bounds.maxLat as number, bounds.maxLong as number] as LatLngTuple,
  ]);
};

const defaultCenter: LatLngTuple = [62.3919741, 15.0685715];

export const Map = (props: Props) => {
    const {
      height,
      width,
      defaultZoom = 4,
      viewCenter,
      children,
    } = props;

    const style = {height, width};

    const centerProps: MapProps = {};

    if (viewCenter) {
      centerProps.center = [viewCenter.latitude, viewCenter.longitude];
    } else {
      if (children && children.props && children.props.markers) {
        const bounds = boundsFromMarkers(children.props.markers);
        if (bounds.isJust()) {
          centerProps.bounds = bounds.get();
        } else {
          centerProps.center = defaultCenter;
        }
      } else {
        centerProps.center = defaultCenter;
      }
    }

    return (
      <Column>
        <LeafletMap
          maxZoom={18}
          minZoom={3}
          zoom={defaultZoom}
          className="Map"
          scrollWheelZoom={false}
          onclick={toggleScrollWheelZoom}
          style={style}
          {...centerProps}
        >
          <TileLayer url="https://{s}.tile.openstreetmap.se/hydda/full/{z}/{x}/{y}.png"/>
          {children}
        </LeafletMap>
      </Column>
    );
  }
;

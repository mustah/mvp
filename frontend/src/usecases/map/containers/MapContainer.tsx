import 'leaflet.markercluster/dist/MarkerCluster.css';
import 'leaflet.markercluster/dist/MarkerCluster.Default.css';
import 'leaflet/dist/leaflet.css';
import * as React from 'react';
import {Map as LeafletMap, TileLayer} from 'react-leaflet';
import {connect} from 'react-redux';
import {Column} from '../../../components/layouts/column/Column';
import './MapContainer.scss';
import {GeoPosition} from '../../../state/domain-models/domainModels';

interface StateToProps {
  children?: React.ReactNode;
}

interface OwnProps {
  height?: number;
  width?: number;
  defaultZoom?: number;
  viewCenter?: GeoPosition;
}

const maxZoom = 18;
const minZoom = 3;

class Map extends React.Component<StateToProps & OwnProps, any> {
  render() {
    const {
      height,
      width,
      defaultZoom = 7,
      viewCenter = defaultViewCenter,
      children,
    } = this.props;

    return (
      <Column>
        <LeafletMap
          center={[viewCenter.latitude, viewCenter.longitude]}
          maxZoom={maxZoom}
          minZoom={minZoom}
          zoom={defaultZoom}
          className="Map"
          scrollWheelZoom={false}
          onclick={toggleScrollWheelZoom}
          style={{height, width}}
        >
          <TileLayer
            url="https://{s}.tile.openstreetmap.se/hydda/full/{z}/{x}/{y}.png"
          />
          {children}
        </LeafletMap>
      </Column>
    );
  }

}

const toggleScrollWheelZoom = (e) => {
  if (e.target.scrollWheelZoom.enabled()) {
    e.target.scrollWheelZoom.disable();
  } else {
    e.target.scrollWheelZoom.enable();
  }
};

const defaultViewCenter: GeoPosition = {latitude: 56.142226, longitude: 13.402965, confidence: 1};

const mapStateToProps = (): StateToProps => {
  return {
  };
};

export const MapContainer = connect<StateToProps, {}, OwnProps>(mapStateToProps)(Map);

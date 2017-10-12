import * as React from 'react';
import {Column} from '../../../common/components/layouts/column/Column';
import './Map.scss';
import {Map, Marker, Popup, TileLayer} from 'react-leaflet';
import MarkerClusterGroup from 'react-leaflet-markercluster';

export interface MapProps {
  name?: string;
}

export class MoidMap extends React.Component<MapProps, any> {

  componentDidMount() {
    // const myMap = L.map('dashboardMap').setView([57.504935, 12.069482], 14);
    // TODO The token needs to be replaced with one that allows commercial use
    // const mapToken = 'pk.eyJ1IjoiZGFuc3ZlIiwiYSI6ImNqOGthZmk5azBiaXQydnVhMGI0cHQwaDUifQ.T4PeUHZoHl0dSpjO8RPJiQ';
  }

  clusterClick() {
    alert('Popup goes here');
  }

  render() {
    const position: [number, number] = [57.504935, 12.069482];

    const markers = [
      {lat: 49.8397, lng: 24.0297},
      {lat: 49.8394, lng: 24.0294},
      {lat: 49.7394, lng: 24.0274},
      {lat: 47.7394, lng: 23.0274},
      {lat: 44.7394, lng: 23.0274},
      {lat: 52.2297, lng: 21.0122},
      {lat: 51.5074, lng: -0.0901},
    ];

    return (
      <Column>
        {/*TODO move this*/}
        <link rel="stylesheet" href="http://cdn.leafletjs.com/leaflet-0.5/leaflet.css"/>

        <link href="https://leaflet.github.io/Leaflet.markercluster/dist/MarkerCluster.css" rel="stylesheet" />
        <link href="https://leaflet.github.io/Leaflet.markercluster/dist/MarkerCluster.Default.css" rel="stylesheet" />
        <Map center={position} maxZoom={50} zoom={3} className="Map" >
          <TileLayer
            url="http://{s}.tile.osm.org/{z}/{x}/{y}.png"
            attribution="&copy; <a href='http://osm.org/copyright'>OpenStreetMap</a> contributors"
          />
          <MarkerClusterGroup
            onClusterClick={this.clusterClick}
            markers={markers}
            options={{zoomToBoundsOnClick: false}}
            wrapperOptions={{enableDefaultStyle: false}}
          />
          <Marker position={position}>
            <Popup>
              <span>A pretty CSS3 popup.<br/>Easily customizable.</span>
            </Popup>
          </Marker>
        </Map>
      </Column>
    );
  }
}

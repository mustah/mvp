import * as React from 'react';
import {Column} from '../../../common/components/layouts/column/Column';
import './Map.scss';
import {Circle, Map, Marker, Popup, TileLayer} from 'react-leaflet';

export interface MapProps {
  name?: string;
}

export class MoidMap extends React.Component<MapProps, any> {

  componentDidMount() {
    // const myMap = L.map('dashboardMap').setView([57.504935, 12.069482], 14);
    // TODO The token needs to be replaced with one that allows commercial use
    // const mapToken = 'pk.eyJ1IjoiZGFuc3ZlIiwiYSI6ImNqOGthZmk5azBiaXQydnVhMGI0cHQwaDUifQ.T4PeUHZoHl0dSpjO8RPJiQ';
  }

  render() {
    const position: [number, number] = [57.504935, 12.069482];
    const position2: [number, number] = [57.500935, 12.060482];

    return (
      <Column>aaa
        {/*TODO move this!*/}
        <link rel="stylesheet" href="http://cdn.leafletjs.com/leaflet-0.5/leaflet.css"/>
        <Map center={position} zoom={13} className="Map" >
          <TileLayer
            url="http://{s}.tile.osm.org/{z}/{x}/{y}.png"
            attribution="&copy; <a href='http://osm.org/copyright'>OpenStreetMap</a> contributors"
          />
          <Marker position={position}>
            <Popup>
              <span>A pretty CSS3 popup.<br/>Easily customizable.</span>
            </Popup>
          </Marker>
          <Circle center={position2} radius={100}>
            <Popup>
              <span>A pretty CSS3 popup.<br/>Easily customizable.</span>
            </Popup>
          </Circle>
        </Map>bbb
      </Column>
    );
  }
}

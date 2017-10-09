import * as React from 'react';
import {Column} from '../../../common/components/layouts/column/Column';
import './Map.scss';
import * as L from 'leaflet';

export interface MapProps {
  name?: string;
}

export class Map extends React.Component<MapProps, any> {
  componentDidMount() {
    let myMap = L.map('dashboardMap').setView([57.504935, 12.069482], 14);
    L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoiZGFuc3ZlIiwiYSI6ImNqOGthZmk5azBiaXQydnVhMGI0cHQwaDUifQ.T4PeUHZoHl0dSpjO8RPJiQ', {
      attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://mapbox.com">Mapbox</a>',
      maxZoom: 18,
      id: 'mapbox.streets',
      accessToken: 'your.mapbox.access.token'
    }).addTo(myMap);

    var marker = L.marker([57.504935, 12.069482]).addTo(myMap);

  }

  render() {
    return (
      <Column>
        <link rel="stylesheet" href="http://cdn.leafletjs.com/leaflet-0.5/leaflet.css"/>
        <div id="dashboardMap" className="Map"/>
      </Column>
    );
  }
}


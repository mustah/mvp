import * as React from 'react';
import {Column} from '../../../common/components/layouts/column/Column';
import './Map.scss';
import * as L from 'leaflet';

export interface MapProps {
  name?: string;
}

export class Map extends React.Component<MapProps, any> {
  componentDidMount() {
    const myMap = L.map('dashboardMap').setView([57.504935, 12.069482], 14);

    // TODO The token needs to be replaced with one that allows commercial use
    L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoiZGFuc3ZlIiwiYSI6ImNqOGthZmk5azBiaXQydnVhMGI0cHQwaDUifQ.T4PeUHZoHl0dSpjO8RPJiQ', {
      attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://mapbox.com">Mapbox</a>',
      maxZoom: 18,
      id: 'mapbox.streets',
      accessToken: 'your.mapbox.access.token',
    }).addTo(myMap);

    // TODO all this is mockup!
    let circle = L.circle([57.506935, 12.058482], {
      color: 'orange',
      fillColor: 'orange',
      fillOpacity: 1,
      radius: 20,
    });
    circle.addTo(myMap).bindPopup('<b>Somethings fishy!</b><br>Meter has not reported any values in 38 hours');

    L.circle([57.506635, 12.04482], 20, {
      color: 'red',
      fillColor: 'red',
      fillOpacity: 1,
    }).addTo(myMap).bindPopup('I am a circle.');

    circle = L.circle([57.506935, 12.079482], {
      color: '#070',
      fillColor: '#070',
      fillOpacity: 1,
      radius: 20,
    });
    circle.bindPopup('<b>Living la vida loca!</b><br>Life is good!');
    circle.addTo(myMap);

    circle = L.circle([57.504935, 12.079482], {
      color: '#f03',
      fillColor: '#f03',
      fillOpacity: 1,
      radius: 20,
    });
    circle.bindPopup('<b>Error!</b><br>Broken!');
    circle.addTo(myMap);

    // const popup = L.popup();
    //
    // function onMapClick(e) {
    //   popup
    //     .setLatLng(e.latlng)
    //     .setContent("You clicked the map at " + e.latlng.toString())
    //     .openOn(myMap);
    // }

    // myMap.on('click', onMapClick);

    const marker = L.marker([57.504935, 12.069482]);
    marker.addTo(myMap);
    marker.bindPopup('<b>Elvaco</b><br>Center of the world as we know it!');

  }

  render() {
    return (
      <Column>
        {/*TODO move this!*/}
        <link rel="stylesheet" href="http://cdn.leafletjs.com/leaflet-0.5/leaflet.css"/>
        <div id="dashboardMap" className="Map"/>
      </Column>
    );
  }
}

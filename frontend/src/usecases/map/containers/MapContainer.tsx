import * as L from 'leaflet';
import 'leaflet.markercluster/dist/MarkerCluster.css';
import 'leaflet.markercluster/dist/MarkerCluster.Default.css';
import 'leaflet/dist/leaflet.css';
import * as React from 'react';
import {Map, TileLayer} from 'react-leaflet';
import MarkerClusterGroup from 'react-leaflet-markercluster';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {Column} from '../../common/components/layouts/column/Column';
import '../Map.scss';
import {MapState} from '../mapReducer';
import {openClusterDialog, toggleClusterDialog} from '../mapActions';
import {MapMarker} from '../mapModels';
import {MeteringPointDialog} from '../../metering-point/MeteringPointDialog';

interface StateToProps {
  map: MapState;
  children?: React.ReactNode;
}

interface DispatchToProps {
  toggleClusterDialog: () => any;
  openClusterDialog: (marker: L.Marker) => any;
}

interface OwnProps {
  markers: { [key: string]: MapMarker };
}

class MapContainer extends React.Component<StateToProps & DispatchToProps & OwnProps, any> {
  render() {
    const {
      toggleClusterDialog,
      map,
      markers,
      openClusterDialog,
    } = this.props;

    const maxZoom = 18;
    const minZoom = 3;
    const defaultZoom = 7;

    const markerclusterOptions = {
      // Setting custom icon for cluster group
      iconCreateFunction: (cluster) => {
        // TODO Test performance!
        // TODO Find status of the marker instead of guessing by checking iconUrl
        // Set cluster css class depending on underlying marker icons
        let cssClass = '';
        let errorCount = 0;
        let warningCount = 0;
        for (const child of cluster.getAllChildMarkers()) {
          if (child.options.icon.options.iconUrl === 'marker-icon-error.png') {
            errorCount++;
          } else if (child.options.icon.options.iconUrl === 'marker-icon-warning.png') {
            warningCount++;
          }
        }

        let x = cluster.getChildCount() / 9;
        if (x > 90) {
          x = 100;
        } else if (x < 30) {
          x = 30;
        }

        let percent = (cluster.getChildCount() - errorCount - warningCount) / cluster.getChildCount() * 100;
        percent = Math.round(percent);

        if (percent === 100) {
          cssClass = 'marker-cluster-ok';
        } else if (percent > 90) {
          cssClass = 'marker-cluster-warning';
        } else {
          cssClass = 'marker-cluster-error';
        }

        return L.divIcon({
          html: `<span>${cluster.getChildCount()}</span>`,
          className: cssClass,
          iconSize: L.point(x, x, true),
        });
      },
      chunkedLoading: true,
      showCoverageOnHover: true,
      maxClusterRadius: (currentZoom) => {
        if (currentZoom < maxZoom) {
          return 80;
        } else {
          return 5;
        }
      },
    };

    const startPosition: [number, number] = [57.504935, 12.069482];
    const confidenceThreshold: number = 0.7;
    // TODO type array
    const leafletMarkers: any[] = [];
    let tmpIcon;

    // TODO break up marker icon logic into methods and add tests

    Object.keys(markers).forEach((key: string) => {
      const marker = markers[key];

      switch (marker.status) {
        case '0': {
          tmpIcon = 'marker-icon-ok.png';
          break;
        }
        case '1': {
          tmpIcon = 'marker-icon-warning.png';
          break;
        }
        case '2': {
          tmpIcon = 'marker-icon-error.png';
          break;
        }
        default: {
          tmpIcon = 'marker-icon.png';
        }
      }

      const {latitude, longitude, confidence} = marker.position;
      if (latitude && longitude && confidence >= confidenceThreshold) {
        leafletMarkers.push(
          {
            lat: latitude,
            lng: longitude,
            options: {
              icon: L.icon({
                iconUrl: tmpIcon,
              }),
            },
            status,
          },
        );
      }
    });

    const toggleScrollWheelZoom = (e) => {
      if (e.target.scrollWheelZoom.enabled()) {
        e.target.scrollWheelZoom.disable();
      } else {
        e.target.scrollWheelZoom.enable();
      }
    };

    return (
      <Column>
        <Map
          center={startPosition}
          maxZoom={maxZoom}
          minZoom={minZoom}
          zoom={defaultZoom}
          className="Map"
          scrollWheelZoom={false}
          onclick={toggleScrollWheelZoom}
        >
          <TileLayer
            url="https://{s}.tile.openstreetmap.se/hydda/full/{z}/{x}/{y}.png"
            attribution="&copy; <a href='http://osm.org/copyright'>OpenStreetMap</a> contributors"
          />
          <MarkerClusterGroup
            markers={leafletMarkers}
            onMarkerClick={openClusterDialog}
            options={markerclusterOptions}
          />
        </Map>
        <MeteringPointDialog displayDialog={map.isClusterDialogOpen} close={toggleClusterDialog}/>
      </Column>
    );
  }
}

const mapStateToProps = ({map}: RootState): StateToProps => {
  return {
    map,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  toggleClusterDialog,
  openClusterDialog,
}, dispatch);

export default connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(MapContainer);

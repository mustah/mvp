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
import {GatewayDialogContainer} from '../../common/containers/dialogs/GatewayDialogContainer';
import {MeteringPointDialogContainer} from '../../common/containers/dialogs/MeteringPointDialogContainer';
import {Column} from '../../common/components/layouts/column/Column';
import {openClusterDialog, toggleClusterDialog} from '../mapActions';
import {MapMarker} from '../mapModels';
import {MapState} from '../mapReducer';
import './MapContainer.scss';
import {isNullOrUndefined} from 'util';
import {Gateway} from '../../../state/domain-models/gateway/gatewayModels';
import {Meter} from '../../../state/domain-models/meter/meterModels';
import {GeoPosition} from '../../../state/domain-models/domainModels';

interface StateToProps {
  map: MapState;
  children?: React.ReactNode;
}

interface DispatchToProps {
  toggleClusterDialog: () => any;
  openClusterDialog: (marker: L.Marker) => any;
}

interface OwnProps {
  /* TODO MapContainer shouldn't even need to know that there is a popup,
     it should only need to know what to do when a marker is clicked
  */
  popupMode: PopupMode;
  markers: { [key: string]: MapMarker } | MapMarker;
  height?: number;
  width?: number;
  defaultZoom?: number;
  viewCenter?: GeoPosition;
}

export enum PopupMode {
  gateway,
  meterpoint,
  none,
}

class MapContainer extends React.Component<StateToProps & DispatchToProps & OwnProps, any> {
  render() {
    const {
      toggleClusterDialog,
      map,
      markers,
      openClusterDialog,
      popupMode,
      height,
      width,
      defaultZoom = 7,
      viewCenter = defaultViewCenter,
    } = this.props;

    let tmpMarkers: { [key: string]: MapMarker } = {};
    if (isMapMarker(markers)) {
      tmpMarkers[0] = markers;
    } else {
      tmpMarkers = markers;
    }

    const maxZoom = 18;
    const minZoom = 3;

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
        percent = Math.floor(percent);

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

    const confidenceThreshold: number = 0.7;
    // TODO type array
    const leafletMarkers: any[] = [];
    let tmpIcon;

    // TODO break up marker icon logic into methods and add tests

    Object.keys(tmpMarkers).forEach((key: string) => {
      const marker = tmpMarkers[key];

      // TODO This logic is currently very fragile. We don't know every possible status, and how severe that status is.
      switch (marker.status.id) {
        case 0:
        case 1:
          tmpIcon = 'marker-icon-ok.png';
          break;
        case 2:
          tmpIcon = 'marker-icon-warning.png';
          break;
        case 3:
          tmpIcon = 'marker-icon-error.png';
          break;
        default:
          tmpIcon = 'marker-icon.png';
          break;
      }

      const {latitude, longitude, confidence} = marker.position;

      if (latitude && longitude && confidence >= confidenceThreshold) {
        leafletMarkers.push({
            lat: latitude,
            lng: longitude,
            options: {
              icon: L.icon({
                iconUrl: tmpIcon,
              }),
              mapMarker: marker,
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

    let popup;

    if (!isNullOrUndefined(map.selectedMarker) && map.selectedMarker.options) {
      if (popupMode === PopupMode.gateway) {
        popup = map.isClusterDialogOpen && (
          <GatewayDialogContainer
            gateway={map.selectedMarker.options.mapMarker as Gateway}
            displayDialog={map.isClusterDialogOpen}
            close={toggleClusterDialog}
          />
        );
      } else if (popupMode === PopupMode.meterpoint) {
        popup = map.isClusterDialogOpen && (
          <MeteringPointDialogContainer
            meter={map.selectedMarker.options.mapMarker as Meter}
            displayDialog={map.isClusterDialogOpen}
            close={toggleClusterDialog}
          />
        );
      }
    }

    return (
      <Column>
        <Map
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
          <MarkerClusterGroup
            markers={leafletMarkers}
            onMarkerClick={openClusterDialog}
            options={markerclusterOptions}
          />
        </Map>
        {popup}
      </Column>
    );
  }
}

const isMapMarker = (obj: any): obj is MapMarker => {
  return obj.status !== undefined && obj.position !== undefined;
};

const defaultViewCenter: GeoPosition = {latitude: 56.142226, longitude: 13.402965, confidence: 1};

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

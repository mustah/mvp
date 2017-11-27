import 'leaflet.markercluster/dist/MarkerCluster.css';
import 'leaflet.markercluster/dist/MarkerCluster.Default.css';
import 'leaflet/dist/leaflet.css';
import * as React from 'react';
import {Map, TileLayer} from 'react-leaflet';
import {connect} from 'react-redux';
import {RootState} from '../../../reducers/rootReducer';
import {GatewayDialogContainer} from '../../../containers/dialogs/GatewayDialogContainer';
import {MeteringPointDialogContainer} from '../../../containers/dialogs/MeteringPointDialogContainer';
import {Column} from '../../../components/layouts/column/Column';
import {toggleClusterDialog} from '../mapActions';
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

interface OwnProps {
  /* TODO MapContainer shouldn't even need to know that there is a popup,
     it should only need to know what to do when a marker is clicked
  */
  popupMode: PopupMode;
  height?: number;
  width?: number;
  defaultZoom?: number;
  viewCenter?: GeoPosition;
}

export const enum PopupMode {
  gateway,
  meterpoint,
  none,
}

const maxZoom = 18;
const minZoom = 3;

class MapContainer extends React.Component<StateToProps & OwnProps, any> {
  render() {
    const {
      map,
      popupMode,
      height,
      width,
      defaultZoom = 7,
      viewCenter = defaultViewCenter,
      children,
    } = this.props;
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
          {children}
        </Map>
        {map.isClusterDialogOpen && this.renderDialog()}
      </Column>
    );
  }

  renderDialog = () => {
    const {map, toggleClusterDialog} = this.props;
    if (map.selectedMarker && map.selectedMarker.options) {
      const {selectedMarker: {options: {mapMarker}}} = map;
      return (
        <Dialog isOpen={map.isClusterDialogOpen} close={toggleClusterDialog}>
          {this.renderDialogContent(mapMarker)}
        </Dialog>
      );
    }
    return null;
  }

  renderDialogContent = (mapMarker: Gateway | Meter) => {
    const {popupMode} = this.props;
    if (popupMode === PopupMode.gateway) {
      return <GatewayDetailsContainer gateway={mapMarker as Gateway}/>;
    } else {
      return <MeterDetailsContainer meter={mapMarker as Meter}/>;
    }
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

const mapStateToProps = ({map}: RootState): StateToProps => {
  return {
    map,
  };
};

export default connect<StateToProps, null, OwnProps>(mapStateToProps)(MapContainer);

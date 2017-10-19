import * as React from 'react';
import {Column} from '../../../../common/components/layouts/column/Column';
import '../Map.scss';
import {Map, TileLayer} from 'react-leaflet';
import {Marker, marker} from 'leaflet';
import MarkerClusterGroup from 'react-leaflet-markercluster';
import Dialog from 'material-ui/Dialog';
import {FlatButton} from 'material-ui';
import {bindActionCreators} from 'redux';
import {fetchPositions, openClusterDialog, toggleClusterDialog} from '../MapActions';
import {RootState} from '../../../../../reducers/index';
import {MapState} from '../MapReducer';
import {connect} from 'react-redux';
import {translate} from '../../../../../services/translationService';
import * as L from 'leaflet';

interface MapContainerProps {
  map: MapState;
  children?: React.ReactNode;
}

interface MapDispatchToProps {
  toggleClusterDialog: () => any;
  openClusterDialog: (marker: Marker) => any;
  fetchPositions: () => any;
}

class MapContainer extends React.Component<MapContainerProps & MapDispatchToProps, any> {
  componentDidMount() {
    this.props.fetchPositions();
  }

  render() {
      const {
        toggleClusterDialog,
        map,
        openClusterDialog,

      } = this.props;

      const actions = [
        (
          <FlatButton
            label={translate('close')}
            primary={true}
            onClick={toggleClusterDialog}
            keyboardFocused={true}
            key={1}
          />
        ),
      ];

    const startPosition: [number, number] = [57.504935, 12.069482];

    let markers = [];
    let tmpIcon;

    //TODO break up marker icon logic into methods and add tests

    if(map != null && map.moids != null) {
      for(var x = 0; x < map.moids.length; x++) {

        //TODO change status to a enumeration!
        switch (map.moids[x].status) {
          case 0: {
            tmpIcon = 'marker-icon-ok.png';
            break;
          };
          case 1: {
            tmpIcon = 'marker-icon-error.png';
            break;
          }
          case 2: {
            tmpIcon = 'marker-icon-warning.png';
            break;
          }
          default: {
            tmpIcon = 'marker-icon.png';
          }
        }

        markers.push(
          {
            lat: map.moids[x].position.lat,
            lng: map.moids[x].position.lng,
            options: { icon: L.icon({
              iconUrl: tmpIcon,
            }) }
          }
          );
      }
    }
    return (
        <Column>
          {/*TODO move this*/}
          <link rel="stylesheet" href="//cdn.leafletjs.com/leaflet-0.5/leaflet.css"/>

          <link href="https://leaflet.github.io/Leaflet.markercluster/dist/MarkerCluster.css" rel="stylesheet"/>
          <link href="https://leaflet.github.io/Leaflet.markercluster/dist/MarkerCluster.Default.css" rel="stylesheet"/>
          <Map center={startPosition} maxZoom={50} zoom={3} className="Map">
            <TileLayer
              url="http://{s}.tile.osm.org/{z}/{x}/{y}.png"
              attribution="&copy; <a href='http://osm.org/copyright'>OpenStreetMap</a> contributors"
            />
            <MarkerClusterGroup
              markers={markers}
              onMarkerClick={openClusterDialog}
            />
          </Map>
          <Dialog
            title="Scrollable Dialog"
            actions={actions}
            modal={false}
            open={map.isClusterDialogOpen}
            onRequestClose={toggleClusterDialog}
            autoScrollBodyContent={true}
          >
            {map.selectedMarker ? map.selectedMarker.getLatLng().toString() : null}
          </Dialog>
        </Column>
      );
    }
  }

const mapStateToProps = (state: RootState) => {
  const {map} = state;

  return {
    map,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
    toggleClusterDialog,
    openClusterDialog,
    fetchPositions,
  }, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(MapContainer);

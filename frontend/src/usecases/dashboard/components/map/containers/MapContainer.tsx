import * as React from 'react';
import {Column} from '../../../../common/components/layouts/column/Column';
import '../Map.scss';
import {Map, Marker, Popup, TileLayer} from 'react-leaflet';
import MarkerClusterGroup from 'react-leaflet-markercluster';
import Dialog from 'material-ui/Dialog';
import {FlatButton} from 'material-ui';
import {bindActionCreators} from 'redux';
import {toggleClusterDialog} from '../MapActions';
import {RootState} from '../../../../../reducers/index';
import {MapState} from '../MapReducer';
import {connect} from 'react-redux';
import {translate} from '../../../../../services/translationService';

interface MapContainerProps {
  map: MapState;
  toggleClusterDialog: () => void;
}

const MapContainer = (props: MapContainerProps) => {
    const actions = [
      (
        <FlatButton
          label={translate('close')}
          primary={true}
          onClick={props.toggleClusterDialog}
          keyboardFocused={true}
          key={1}
        />
      ),
    ];

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
        <link rel="stylesheet" href="//cdn.leafletjs.com/leaflet-0.5/leaflet.css"/>

        <link href="https://leaflet.github.io/Leaflet.markercluster/dist/MarkerCluster.css" rel="stylesheet" />
        <link href="https://leaflet.github.io/Leaflet.markercluster/dist/MarkerCluster.Default.css" rel="stylesheet" />
        <Map center={position} maxZoom={50} zoom={3} className="Map" >
          <TileLayer
            url="http://{s}.tile.osm.org/{z}/{x}/{y}.png"
            attribution="&copy; <a href='http://osm.org/copyright'>OpenStreetMap</a> contributors"
          />
          <MarkerClusterGroup
            onClusterClick={props.toggleClusterDialog}
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
        <Dialog
          title="Scrollable Dialog"
          actions={actions}
          modal={false}
          open={props.map.isClusterDialogOpen}
          onRequestClose={props.toggleClusterDialog}
          autoScrollBodyContent={true}
        >
          Test
        </Dialog>
      </Column>
    );
  };

const mapStateToProps = (state: RootState) => {
  const {map} = state;

  return {
    map,
    isClusterDialogOpen: map.isClusterDialogOpen,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
    toggleClusterDialog,
  }, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(MapContainer);

import * as React from 'react';
import {Column} from '../../../../common/components/layouts/column/Column';
import '../Map.scss';
import {Map, TileLayer} from 'react-leaflet';
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
            markers={props.map.markerPositions}
            wrapperOptions={{enableDefaultStyle: false}}
            onMarkerClick={(marker) => props.map.selectedMarker = marker}
          />
        </Map>
        <Dialog
          title="Scrollable Dialog"
          actions={actions}
          modal={false}
          open={props.map.isClusterDialogOpen}
          onRequestClose={props.toggleClusterDialog}
          autoScrollBodyContent={true}
        >
          {props.map.selectedMarker}
        </Dialog>
      </Column>
    );
  };

const mapStateToProps = (state: RootState) => {
  const {map} = state;

  return {
    map,
    markerPositions: map.markerPositions,
    selectedMarker: map.selectedMarker,
    isClusterDialogOpen: map.isClusterDialogOpen,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
    toggleClusterDialog,
  }, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(MapContainer);

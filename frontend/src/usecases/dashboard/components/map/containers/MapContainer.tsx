import * as React from 'react';
import {Column} from '../../../../common/components/layouts/column/Column';
import '../Map.scss';
import {Map, TileLayer} from 'react-leaflet';
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
  openClusterDialog: (marker: L.Marker) => any;
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

    const markerclusterOptions = {
      // Setting custom icon for cluster group
      iconCreateFunction: (cluster) => {
        // TODO Test performance!
        // TODO Find status of the marker instead of guessing by checking iconUrl
        // Set cluster css class depending on underlying marker icons
        let cssClass = '';
        for (const child of cluster.getAllChildMarkers()) {
          if (child.options.icon.options.iconUrl === 'marker-icon-error.png') {
            cssClass = 'marker-cluster-custom-error';
            break;
          } else if (child.options.icon.options.iconUrl === 'marker-icon-warning.png') {
            cssClass = 'marker-cluster-custom-warning';
          } else if (cssClass === '') {
            cssClass = 'marker-cluster-custom-ok';
          }
        }

        return L.divIcon({
          html: `<span>${cluster.getChildCount()}</span>`,
          className: cssClass,
          iconSize: L.point(40, 40, true),
        });
      },
      chunkedLoading: true,
      showCoverageOnHover: true,
    };

    const startPosition: [number, number] = [57.504935, 12.069482];

    // TODO type array
    const markers: any[] = [];
    let tmpIcon;

    // TODO break up marker icon logic into methods and add tests

    if (map != null && map.moids != null) {
      for (const moid of map.moids) {

        // TODO change status to a enumeration!
        switch (moid.status) {
          case 0: {
            tmpIcon = 'marker-icon-ok.png';
            break;
          }
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
            lat: moid.position.lat,
            lng: moid.position.lng,
            options: {
              icon: L.icon({
                iconUrl: tmpIcon,
              }),
            },
            status: moid.status,
          },
        );
      }
    }
    return (
      <Column>
        {/* TODO move this*/}
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
            options={markerclusterOptions}
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

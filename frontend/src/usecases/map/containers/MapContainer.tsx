import * as L from 'leaflet';
import 'leaflet.markercluster/dist/MarkerCluster.css';
import 'leaflet.markercluster/dist/MarkerCluster.Default.css';
import 'leaflet/dist/leaflet.css';
import {FlatButton} from 'material-ui';
import Dialog from 'material-ui/Dialog';
import * as React from 'react';
import {Map, TileLayer} from 'react-leaflet';
import MarkerClusterGroup from 'react-leaflet-markercluster';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {getEncodedUriParameters} from '../../../state/search/selection/selectionSelectors';
import {Column} from '../../common/components/layouts/column/Column';
import '../Map.scss';
import {fetchPositions, openClusterDialog, toggleClusterDialog} from '../mapActions';
import {MapState} from '../mapReducer';

interface MapContainerProps {
  map: MapState;
  children?: React.ReactNode;
  encodedUriParameters: string;
}

interface MapDispatchToProps {
  toggleClusterDialog: () => any;
  openClusterDialog: (marker: L.Marker) => any;
  fetchPositions: (encodedUriParameters: string) => any;
}

class MapContainer extends React.Component<MapContainerProps & MapDispatchToProps, any> {
  componentDidMount() {
    const {fetchPositions, encodedUriParameters} = this.props;
    fetchPositions(encodedUriParameters);
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
    };

    const startPosition: [number, number] = [57.504935, 12.069482];
    const confidenceThreshold: number = 0.7;
    // TODO type array
    const markers: any[] = [];
    let tmpIcon;

    // TODO break up marker icon logic into methods and add tests

    map.moids.forEach(moid => {
      const {status} = moid;
      switch (status) {
        case 0: {
          tmpIcon = 'marker-icon-ok.png';
          break;
        }
        case 1: {
          tmpIcon = 'marker-icon-warning.png';
          break;
        }
        case 2: {
          tmpIcon = 'marker-icon-error.png';
          break;
        }
        default: {
          tmpIcon = 'marker-icon.png';
        }
      }

      const {latitude, longitude, confidence} = moid.position;
      if (latitude && longitude && confidence >= confidenceThreshold) {
        markers.push(
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
          maxZoom={19}
          zoom={7}
          className="Map"
          scrollWheelZoom={false}
          onclick={toggleScrollWheelZoom}
        >
          <TileLayer
            url="https://{s}.tile.openstreetmap.se/hydda/full/{z}/{x}/{y}.png"
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

const mapStateToProps = ({map, searchParameters}: RootState): MapContainerProps => {
  return {
    map,
    encodedUriParameters: getEncodedUriParameters(searchParameters),
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  toggleClusterDialog,
  openClusterDialog,
  fetchPositions,
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(MapContainer);

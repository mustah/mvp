import MarkerClusterGroup from 'react-leaflet-markercluster';
import * as L from 'leaflet';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {openClusterDialog, toggleClusterDialog} from '../mapActions';
import {ExtendedMarker, MapMarker} from '../mapModels';
import {IdNamed} from '../../../types/Types';

interface DispatchToProps {
  toggleClusterDialog: () => any;
  openClusterDialog: (marker: ExtendedMarker) => any;
}

interface OwnProps {
   markers: { [key: string]: MapMarker } | MapMarker;
 }

class ClusterContainer extends React.Component<DispatchToProps & OwnProps, any> {
  render() {
    const {
      openClusterDialog,
      markers,
    } = this.props;

    let onMarkerClick = openClusterDialog;

    // if (popupMode === PopupMode.none) {
    //   // Prevent popup in popup
    //   onMarkerClick = () => void(0);
    // }

    let tmpMarkers: { [key: string]: MapMarker } = {};
    if (isMapMarker(markers)) {
      tmpMarkers[0] = markers;
    } else {
      tmpMarkers = markers;
    }

    const confidenceThreshold: number = 0.7;
    // TODO type array
    const leafletMarkers: any[] = [];

    // TODO break up marker icon logic into methods and add tests
    if (tmpMarkers) {
      Object.keys(tmpMarkers).forEach((key: string) => {
        const marker = tmpMarkers[key];
        const {latitude, longitude, confidence} = marker.position;

        if (latitude && longitude && confidence >= confidenceThreshold) {
          leafletMarkers.push({
            lat: latitude,
            lng: longitude,
            options: {
              icon: L.icon({
                iconUrl: getIcon(marker.status),
              }),
              mapMarker: marker,
            },
          });
        }
      });
    }

    const markerclusterOptions = {
      // Setting custom icon for cluster group

      iconCreateFunction: handleIconCreate,
      chunkedLoading: true,
      showCoverageOnHover: true,
      maxClusterRadius: getZoomBasedRadius,
    };

    // const renderCluster = () => leafletMarkers.length > 0 && (
    //   <MarkerClusterGroup
    //     markers={leafletMarkers}
    //     onMarkerClick={onMarkerClick}
    //     options={markerclusterOptions}
    //   />);

    return (
      <MarkerClusterGroup
        markers={leafletMarkers}
        onMarkerClick={onMarkerClick}
        options={markerclusterOptions}
      />
    );
  }
}

const getZoomBasedRadius = (zoom) => {
  if (zoom < maxZoom) {
    return 80;
  } else {
    return 5;
  }
};

const handleIconCreate = (cluster: MarkerClusterGroup) => {
  const x = getCLusterDimensions(cluster.getChildCount());

  return L.divIcon({
    html: `<span>${cluster.getChildCount()}</span>`,
    className: getClusterCssClass(cluster),
    iconSize: L.point(x, x, true),
  });
};

// TODO needs to be shared with MapContainer
const maxZoom = 18;

const getClusterCssClass = (cluster: MarkerClusterGroup): string => {
  // TODO Test performance!
  // TODO Find status of the marker instead of guessing by checking iconUrl
  // Set cluster css class depending on underlying marker icons

  let errorCount = 0;
  let warningCount = 0;
  for (const child of cluster.getAllChildMarkers()) {
    if (child.options.icon.options.iconUrl === 'assets/images/marker-icon-error.png') {
      errorCount++;
    } else if (child.options.icon.options.iconUrl === 'assets/images/marker-icon-warning.png') {
      warningCount++;
    }
  }

  let percent = (cluster.getChildCount() - errorCount - warningCount) / cluster.getChildCount() * 100;
  percent = Math.floor(percent);

  let cssClass: string;
  if (percent === 100) {
    cssClass = 'marker-cluster-ok';
  } else if (percent > 90) {
    cssClass = 'marker-cluster-warning';
  } else {
    cssClass = 'marker-cluster-error';
  }

  return cssClass;
};

const getIcon = (status: IdNamed): string => {
  let tmpIcon: string;

  // TODO This logic is currently very fragile. We don't know every possible status, and how severe that status is.
  switch (status.id) {
    case 0:
    case 1:
      tmpIcon = 'assets/images/marker-icon-ok.png';
      break;
    case 2:
      tmpIcon = 'assets/images/marker-icon-warning.png';
      break;
    case 3:
      tmpIcon = 'assets/images/marker-icon-error.png';
      break;
    default:
      tmpIcon = 'assets/images/marker-icon.png';
      break;
  }

  return tmpIcon;
};

const getCLusterDimensions = (clusterCount: number): number => {
  let x = clusterCount / 9;

  if (x > 90) {
    x = 100;
  } else if (x < 30) {
    x = 30;
  }

  return x;
};

const isMapMarker = (obj: any): obj is MapMarker => {
  return obj && obj.status !== undefined && obj.position !== undefined;
};

const mapDispatchToProps = dispatch => bindActionCreators({
  toggleClusterDialog,
  openClusterDialog,
}, dispatch);

export default connect<{}, DispatchToProps, OwnProps>(null, mapDispatchToProps)(ClusterContainer);

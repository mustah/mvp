import * as React from 'react';
import {Row} from '../../../../components/layouts/row/Row';
import {Widget} from './Widget';
import {MapMarker} from '../../../map/mapModels';
import {MapState} from '../../../map/mapReducer';
import {GeoPosition} from '../../../../state/domain-models/domainModels';
import {Meter} from '../../../../state/domain-models/meter/meterModels';
import {bindActionCreators} from 'redux';
import {closeClusterDialog} from '../../../map/mapActions';
import {connect} from 'react-redux';
import {RootState} from '../../../../reducers/rootReducer';
import {OnClick} from '../../../../types/Types';
import {MapContainer} from '../../../map/containers/MapContainer';
import {ClusterContainer} from '../../../map/containers/ClusterContainer';
import {MeterDetailsContainer} from '../../../../containers/dialogs/MeterDetailsContainer';
import {Dialog} from '../../../../components/dialog/Dialog';

interface Props {
  markers: any;
}

interface StateToProps {
  map: MapState;
}

interface DispatchToProps {
  closeClusterDialog: OnClick;
}

const MapWidgets = (props: StateToProps & DispatchToProps & Props) => {
  const {
    markers,
    map,
    closeClusterDialog,
  } = props;

  // TODO retrieve real data
  const markersFailing: { [key: string]: MapMarker } = {};
  markersFailing[0] = {
    status: {id: 3, name: 'Fel'},
    address: {id: '', cityId: '', name: ''},
    city: {id: '', name: ''},
    position: {
      confidence: 1,
      latitude: 56.138288,
      longitude: 13.394854,
    },
  };

  markersFailing[1] = {
    status: {id: 3, name: 'Fel'},
    address: {id: '', cityId: '', name: ''},
    city: {id: '', name: ''},
    position: {
      confidence: 1,
      latitude: 56.552119,
      longitude: 14.137460,
    },
  };

  const centerOfPerstorpMap: GeoPosition = {latitude: 56.138288, longitude: 13.394854, confidence: 1};
  const centerOfErrorMap: GeoPosition = {latitude: 56.228288, longitude: 13.794854, confidence: 1};

  const dialog = map.selectedMarker && map.isClusterDialogOpen ? (
    <Dialog isOpen={map.isClusterDialogOpen} close={closeClusterDialog}>
      <MeterDetailsContainer
        meter={map.selectedMarker.options.mapMarker as Meter}
      />
    </Dialog>
  ) : null;

  return (
    <Row className="MapWidgets">
      <Widget title="Perstorp">
        <MapContainer
          height={400}
          width={400}
          defaultZoom={13}
          viewCenter={centerOfPerstorpMap}
        >
          <ClusterContainer markers={markers}/>
        </MapContainer>
      </Widget>
      <Widget title="Fel">
        <MapContainer
          height={400}
          width={400}
          defaultZoom={8}
          viewCenter={centerOfErrorMap}
        >
          <ClusterContainer markers={markersFailing}/>
        </MapContainer>
      </Widget>
      {dialog}
    </Row>
  );
};

const mapStateToProps = ({map}: RootState): StateToProps => {
  return {
    map,
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  closeClusterDialog,
}, dispatch);

export const MapWidgetsContainer = connect<StateToProps, DispatchToProps>(
  mapStateToProps, mapDispatchToProps)(MapWidgets);

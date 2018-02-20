import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {HasContent} from '../../../../components/content/HasContent';
import {Dialog} from '../../../../components/dialog/Dialog';
import {Row} from '../../../../components/layouts/row/Row';
import {MissingDataTitle} from '../../../../components/texts/Titles';
import {MeterDetailsContainer} from '../../../../containers/dialogs/MeterDetailsContainer';
import {RootState} from '../../../../reducers/rootReducer';
import {firstUpperTranslated} from '../../../../services/translationService';
import {Meter} from '../../../../state/domain-models-paginated/meter/meterModels';
import {GeoPosition} from '../../../../state/domain-models/domainModels';
import {OnClick} from '../../../../types/Types';
import {ClusterContainer} from '../../../map/containers/ClusterContainer';
import {isMarkersWithinThreshold} from '../../../map/containers/clusterHelper';
import {Map} from '../../../map/containers/Map';
import {closeClusterDialog} from '../../../map/mapActions';
import {MapMarker} from '../../../map/mapModels';
import {MapState} from '../../../map/mapReducer';
import {Widget} from './Widget';

interface OwnProps {
  markers: {[key: string]: MapMarker};
}

interface StateToProps {
  map: MapState;
}

interface DispatchToProps {
  closeClusterDialog: OnClick;
}

type Props = StateToProps & DispatchToProps & OwnProps;

const MapWidgets = ({markers, map, closeClusterDialog}: Props) => {
  // TODO retrieve real data
  const markersFailing: {[key: string]: MapMarker} = {
    0: {
      status: {id: 3, name: 'Fel'},
      address: {id: '', cityId: '', name: ''},
      city: {id: '', name: ''},
      position: {
        confidence: 1,
        latitude: 56.138288,
        longitude: 13.394854,
      },
    },
    1: {
      status: {id: 3, name: 'Fel'},
      address: {id: '', cityId: '', name: ''},
      city: {id: '', name: ''},
      position: {
        confidence: 1,
        latitude: 56.552119,
        longitude: 14.137460,
      },
    },
  };

  const hasMeters: boolean = isMarkersWithinThreshold(markers);

  const centerOfPerstorpMap: GeoPosition = {latitude: 56.138288, longitude: 13.394854, confidence: 1};
  const centerOfErrorMap: GeoPosition = {latitude: 56.228288, longitude: 13.794854, confidence: 1};

  const dialog = map.selectedMarker && map.isClusterDialogOpen && (
    <Dialog isOpen={map.isClusterDialogOpen} close={closeClusterDialog}>
      <MeterDetailsContainer meter={map.selectedMarker as Meter}/>
    </Dialog>
  );

  return (
    <Row className="MapWidgets">
      <Widget title="Perstorp">
        <HasContent
          hasContent={hasMeters}
          fallbackContent={<MissingDataTitle title={firstUpperTranslated('no meters')}/>}
        >
          <Map
            height={400}
            width={400}
            defaultZoom={13}
            viewCenter={centerOfPerstorpMap}
          >
            <ClusterContainer markers={markers}/>
          </Map>
        </HasContent>
      </Widget>
      <Widget title="Fel">
        <Map
          height={400}
          width={400}
          defaultZoom={8}
          viewCenter={centerOfErrorMap}
        >
          <ClusterContainer markers={markersFailing}/>
        </Map>
      </Widget>
      {dialog}
    </Row>
  );
};

const mapStateToProps = ({map}: RootState): StateToProps => ({map});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  closeClusterDialog,
}, dispatch);

export const MapWidgetsContainer = connect<StateToProps, DispatchToProps>(
  mapStateToProps, mapDispatchToProps)(MapWidgets);

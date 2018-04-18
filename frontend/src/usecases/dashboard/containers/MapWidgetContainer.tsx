import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {HasContent} from '../../../components/content/HasContent';
import {Dialog} from '../../../components/dialog/Dialog';
import {Row} from '../../../components/layouts/row/Row';
import {MissingDataTitle} from '../../../components/texts/Titles';
import {MeterDetailsContainer} from '../../../containers/dialogs/MeterDetailsContainer';
import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated} from '../../../services/translationService';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {OnClick} from '../../../types/Types';
import {ClusterContainer} from '../../map/containers/ClusterContainer';
import {Map} from '../../map/containers/Map';
import {meterLowConfidenceTextInfo} from '../../map/helper/mapHelper';
import {closeClusterDialog} from '../../map/mapActions';
import {MapMarker} from '../../map/mapModels';
import {MapState} from '../../map/mapReducer';
import {Widget} from '../components/widgets/Widget';

interface OwnProps {
  markers: DomainModel<MapMarker>;
}

interface StateToProps {
  map: MapState;
}

interface DispatchToProps {
  closeClusterDialog: OnClick;
}

type Props = StateToProps & DispatchToProps & OwnProps;

const MapWidget = ({markers, map, closeClusterDialog}: Props) => {

  const dialog = map.selectedMarker && map.isClusterDialogOpen && (
    <Dialog isOpen={map.isClusterDialogOpen} close={closeClusterDialog}>
      <MeterDetailsContainer meterId={map.selectedMarker}/>
    </Dialog>
  );

  return (
    <Row>
      <Widget title={firstUpperTranslated('all meters in selection')} className="MapWidget">
        <HasContent
          hasContent={markers.result.length > 0}
          fallbackContent={<MissingDataTitle title={firstUpperTranslated('no meters')}/>}
        >
          <Map
            width={800}
            height={600}
            lowConfidenceText={meterLowConfidenceTextInfo(markers)}
          >
            <ClusterContainer markers={markers.entities}/>
          </Map>
        </HasContent>
      </Widget>
      {dialog}
    </Row>
  );
};

const mapStateToProps = ({map}: RootState): StateToProps => ({map});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  closeClusterDialog,
}, dispatch);

export const MapWidgetContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(MapWidget);

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
import {Map} from '../../map/components/Map';
import {ClusterContainer} from '../../map/containers/ClusterContainer';
import {closeClusterDialog} from '../../map/mapActions';
import {Bounds, MapMarker} from '../../map/mapModels';
import {MapState} from '../../map/mapReducer';
import {getBounds, getMeterLowConfidenceTextInfo} from '../../map/mapSelectors';
import {WidgetWithTitle} from '../components/widgets/Widget';

interface OwnProps {
  markers: DomainModel<MapMarker>;
}

interface StateToProps {
  map: MapState;
  lowConfidenceText?: string;
  bounds?: Bounds;
}

interface DispatchToProps {
  closeClusterDialog: OnClick;
}

type Props = StateToProps & DispatchToProps & OwnProps;

const MapWidget = ({bounds, lowConfidenceText, markers, map, closeClusterDialog}: Props) => {

  const dialog = map.selectedMarker && map.isClusterDialogOpen && (
    <Dialog isOpen={map.isClusterDialogOpen} close={closeClusterDialog} autoScrollBodyContent={true}>
      <MeterDetailsContainer meterId={map.selectedMarker}/>
    </Dialog>
  );

  return (
    <Row>
      <WidgetWithTitle
        title={firstUpperTranslated('all meters in selection')}
        className="MapWidget"
      >
        <HasContent
          hasContent={markers.result.length > 0}
          fallbackContent={<MissingDataTitle title={firstUpperTranslated('no meters')}/>}
        >
          <Map
            height={600}
            width={774}
            bounds={bounds}
            lowConfidenceText={lowConfidenceText}
          >
            <ClusterContainer markers={markers.entities}/>
          </Map>
        </HasContent>
      </WidgetWithTitle>
      {dialog}
    </Row>
  );
};

const mapStateToProps = (rootState: RootState): StateToProps => {
  const {map, domainModels: {meterMapMarkers}}: RootState = rootState;
  return ({
    map,
    bounds: getBounds(meterMapMarkers),
    lowConfidenceText: getMeterLowConfidenceTextInfo(rootState),
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  closeClusterDialog,
}, dispatch);

export const MapWidgetContainer =
  connect<StateToProps, DispatchToProps>(() => mapStateToProps, mapDispatchToProps)(MapWidget);

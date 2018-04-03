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
import {Dictionary, OnClick} from '../../../../types/Types';
import {ClusterContainer} from '../../../map/containers/ClusterContainer';
import {metersWithinThreshold} from '../../../map/containers/clusterHelper';
import {Map} from '../../../map/containers/Map';
import {closeClusterDialog} from '../../../map/mapActions';
import {MapMarker} from '../../../map/mapModels';
import {MapState} from '../../../map/mapReducer';
import {Widget} from './Widget';

interface OwnProps {
  markers: Dictionary<MapMarker>;
}

interface StateToProps {
  map: MapState;
}

interface DispatchToProps {
  closeClusterDialog: OnClick;
}

type Props = StateToProps & DispatchToProps & OwnProps;

const MapWidgets = ({markers, map, closeClusterDialog}: Props) => {
  const hasMeters: boolean = metersWithinThreshold(markers).length > 0;

  const dialog = map.selectedMarker && map.isClusterDialogOpen && (
    <Dialog isOpen={map.isClusterDialogOpen} close={closeClusterDialog}>
      <MeterDetailsContainer meterId={map.selectedMarker}/>
    </Dialog>
  );

  return (
    <Row className="MapWidgets">
      <Widget title={firstUpperTranslated('all meters in selection')}>
        <HasContent
          hasContent={hasMeters}
          fallbackContent={<MissingDataTitle title={firstUpperTranslated('no meters')}/>}
        >
          <Map
            defaultZoom={7}
            height={400}
            width={400}
          >
            <ClusterContainer markers={markers}/>
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

export const MapWidgetsContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(MapWidgets);

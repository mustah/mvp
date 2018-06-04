import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {Dialog} from '../../../components/dialog/Dialog';
import {withEmptyContent, WithEmptyContentProps} from '../../../components/hoc/withEmptyContent';
import {Row} from '../../../components/layouts/row/Row';
import {Loader} from '../../../components/loading/Loader';
import {MeterDetailsContainer} from '../../../containers/dialogs/MeterDetailsContainer';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated} from '../../../services/translationService';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {getError} from '../../../state/domain-models/domainModelsSelectors';
import {ClearError, ErrorResponse, OnClick} from '../../../types/Types';
import {Map} from '../../map/components/Map';
import {ClusterContainer} from '../../map/containers/ClusterContainer';
import {closeClusterDialog} from '../../map/mapActions';
import {clearErrorMeterMapMarkers} from '../../map/mapMarkerActions';
import {Bounds, MapMarker} from '../../map/mapModels';
import {MapState} from '../../map/mapReducer';
import {getBounds, getMeterLowConfidenceTextInfo} from '../../map/mapSelectors';
import {WidgetWithTitle} from '../components/widgets/Widget';

interface OwnProps {
  markers: DomainModel<MapMarker>;
}

interface MapContentProps {
  bounds?: Bounds;
  lowConfidenceText?: string;
}

interface StateToProps extends MapContentProps {
  error: Maybe<ErrorResponse>;
  isFetching: boolean;
  map: MapState;
}

interface DispatchToProps {
  closeClusterDialog: OnClick;
  clearError: ClearError;
}

type MapContentWrapperProps = MapContentProps & OwnProps & WithEmptyContentProps;

type Props = MapContentProps & OwnProps & StateToProps & DispatchToProps;

const MapContent = ({bounds, lowConfidenceText, markers}: MapContentProps & OwnProps) => (
  <Map
    height={600}
    width={774}
    bounds={bounds}
    lowConfidenceText={lowConfidenceText}
  >
    <ClusterContainer markers={markers.entities}/>
  </Map>
);

const MapContentWrapper = withEmptyContent<MapContentWrapperProps>(MapContent);

const MapWidget =
  ({
    bounds,
    clearError,
    closeClusterDialog,
    error,
    isFetching,
    markers,
    lowConfidenceText,
    map,
  }: Props) => {

    const dialog = map.selectedMarker && map.isClusterDialogOpen && (
      <Dialog isOpen={map.isClusterDialogOpen} close={closeClusterDialog} autoScrollBodyContent={true}>
        <MeterDetailsContainer meterId={map.selectedMarker}/>
      </Dialog>
    );

    const wrapperProps: MapContentWrapperProps = {
      bounds,
      lowConfidenceText,
      markers,
      hasContent: markers.result.length > 0,
      noContentText: firstUpperTranslated('no meters'),
    };

    return (
      <Row>
        <WidgetWithTitle
          title={firstUpperTranslated('all meters in selection')}
          className="MapWidget"
        >
          <Loader isFetching={isFetching} error={error} clearError={clearError}>
            <MapContentWrapper {...wrapperProps}/>
          </Loader>
          {dialog}
        </WidgetWithTitle>
      </Row>
    );
  };

const mapStateToProps = (rootState: RootState): StateToProps => {
  const {map, domainModels: {meterMapMarkers}}: RootState = rootState;
  return ({
    bounds: getBounds(meterMapMarkers),
    error: getError(meterMapMarkers),
    isFetching: meterMapMarkers.isFetching,
    lowConfidenceText: getMeterLowConfidenceTextInfo(rootState),
    map,
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  clearError: clearErrorMeterMapMarkers,
  closeClusterDialog,
}, dispatch);

export const MapWidgetContainer =
  connect<StateToProps, DispatchToProps>(() => mapStateToProps, mapDispatchToProps)(MapWidget);

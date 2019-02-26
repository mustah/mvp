import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {MeterDetailsDialog} from '../../../components/dialog/DetailsDialog';
import {withEmptyContent, WithEmptyContentProps} from '../../../components/hoc/withEmptyContent';
import {Row} from '../../../components/layouts/row/Row';
import {RetryLoader} from '../../../components/loading/Loader';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated} from '../../../services/translationService';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {getError} from '../../../state/domain-models/domainModelsSelectors';
import {GeoPosition} from '../../../state/domain-models/location/locationModels';
import {ClearError, ErrorResponse, OnClick, uuid} from '../../../types/Types';
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
  viewCenter?: GeoPosition;
}

interface StateToProps extends MapContentProps {
  error: Maybe<ErrorResponse>;
  isFetching: boolean;
  map: MapState;
  title: string;
}

interface DispatchToProps {
  closeClusterDialog: OnClick;
  clearError: ClearError;
}

type MapContentWrapperProps = MapContentProps & OwnProps & WithEmptyContentProps;

type Props = MapContentProps & OwnProps & StateToProps & DispatchToProps;

const MapContent = ({bounds, viewCenter, lowConfidenceText, markers}: MapContentProps & OwnProps) => (
  <Map
    height={600}
    width={774}
    bounds={bounds}
    lowConfidenceText={lowConfidenceText}
    viewCenter={viewCenter}
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
    title,
    viewCenter,
  }: Props) => {
    const {isClusterDialogOpen, selectedMarker} = map;
    const selectedId = Maybe.maybe<uuid>(selectedMarker);
    const dialog = selectedId.isJust() && isClusterDialogOpen && (
      <MeterDetailsDialog
        autoScrollBodyContent={true}
        close={closeClusterDialog}
        isOpen={isClusterDialogOpen}
        selectedId={selectedId}
      />
    );

    const wrapperProps: MapContentWrapperProps = {
      bounds,
      lowConfidenceText,
      markers,
      hasContent: markers.result.length > 0,
      noContentText: firstUpperTranslated('no meters'),
      viewCenter,
    };

    return (
      <Row>
        <WidgetWithTitle title={title} className="MapWidget">
          <RetryLoader isFetching={isFetching} error={error} clearError={clearError}>
            <MapContentWrapper {...wrapperProps}/>
          </RetryLoader>
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
    title: firstUpperTranslated('all meters'),
    viewCenter: map.viewCenter,
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  clearError: clearErrorMeterMapMarkers,
  closeClusterDialog,
}, dispatch);

export const MapWidgetContainer =
  connect<StateToProps, DispatchToProps>(() => mapStateToProps, mapDispatchToProps)(MapWidget);

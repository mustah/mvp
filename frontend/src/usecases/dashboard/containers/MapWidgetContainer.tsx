import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {Period} from '../../../components/dates/dateModels';
import {MeterDetailsDialog} from '../../../components/dialog/DetailsDialog';
import {withEmptyContent, WithEmptyContentProps} from '../../../components/hoc/withEmptyContent';
import {Row} from '../../../components/layouts/row/Row';
import {RetryLoader} from '../../../components/loading/Loader';
import {Maybe} from '../../../helpers/Maybe';
import {makeApiParametersOf} from '../../../helpers/urlFactory';
import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated} from '../../../services/translationService';
import {DomainModel, RequestsHttp} from '../../../state/domain-models/domainModels';
import {getErrorCalle} from '../../../state/domain-models/domainModelsSelectors';
import {GeoPosition} from '../../../state/domain-models/location/locationModels';
import {deleteWidget} from '../../../state/domain-models/widget/widgetActions';
import {getMeterParameters} from '../../../state/user-selection/userSelectionSelectors';
import {WidgetMandatory, WidgetType} from '../../../state/widget/configuration/widgetConfigurationReducer';
import {fetchMapWidget, FetchWidgetIfNeeded} from '../../../state/widget/data/widgetDataActions';
import {WidgetData} from '../../../state/widget/data/widgetDataReducer';
import {CallbackWith, ClearError, ErrorResponse, OnClick, uuid} from '../../../types/Types';
import {Map} from '../../map/components/Map';
import {ClusterContainer} from '../../map/containers/ClusterContainer';
import {boundsFromMarkers} from '../../map/helper/mapHelper';
import {closeClusterDialog} from '../../map/mapActions';
import {clearErrorMeterMapMarkers} from '../../map/mapMarkerActions';
import {Bounds, MapMarker} from '../../map/mapModels';
import {MapState} from '../../map/mapReducer';
import {getMeterLowConfidenceTextInfo} from '../../map/mapSelectors';
import {WidgetWithTitle} from '../components/widgets/Widget';

export interface MapWidgetSettings extends WidgetMandatory {
  type: WidgetType.MAP;
  settings: {
    selectionId?: uuid;
  };
}

interface OwnProps {
  settings: MapWidgetSettings;
  openConfiguration: OnClick;
  onDelete: CallbackWith<WidgetMandatory>;
}

interface MapContentProps {
  bounds?: Bounds;
  lowConfidenceText?: string;
  viewCenter?: GeoPosition;
  markers: DomainModel<MapMarker>;
}

interface StateToProps extends MapContentProps {
  model: WidgetData & RequestsHttp;
  error: Maybe<ErrorResponse>;
  isFetching: boolean;
  map: MapState;
  title: string;
  isUserSelectionsSuccessfullyFetched: boolean;
  parameters: string;
}

interface DispatchToProps {
  closeClusterDialog: OnClick;
  clearError: ClearError;
  fetchMapWidget: CallbackWith<FetchWidgetIfNeeded>;
}

type MapContentWrapperProps = MapContentProps & WithEmptyContentProps;

type Props = MapContentProps & OwnProps & StateToProps & DispatchToProps;

const MapContent = ({bounds, viewCenter, lowConfidenceText, markers: {entities}}: MapContentProps) => (
  <Map
    bounds={bounds}
    lowConfidenceText={lowConfidenceText}
    viewCenter={viewCenter}
  >
    <ClusterContainer markers={entities.meterMapMarkers}/>
  </Map>
);

const MapContentWrapper = withEmptyContent<MapContentWrapperProps>(MapContent);

const MapWidget = (props: Props) => {
  const {
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
    isUserSelectionsSuccessfullyFetched,
    fetchMapWidget,
    openConfiguration,
    settings,
    parameters,
    onDelete,
  } = props;

  React.useEffect(() => {
    if (isUserSelectionsSuccessfullyFetched) {
      fetchMapWidget(props);
    }
  }, [settings, parameters, isUserSelectionsSuccessfullyFetched]);

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

  const onClickDeleteWidget = () => onDelete(settings);

  return (
    <Row>
      <WidgetWithTitle
        title={title}
        className="MapWidget"
        configure={openConfiguration}
        deleteWidget={onClickDeleteWidget}
      >
        <RetryLoader isFetching={isFetching} error={error} clearError={clearError}>
          <MapContentWrapper {...wrapperProps}/>
        </RetryLoader>
        {dialog}
      </WidgetWithTitle>
    </Row>
  );
};

const mapStateToProps = (rootState: RootState, ownProps: OwnProps): StateToProps => {
  const {
    map,
    domainModels: {userSelections},
    widget: {data}
  }: RootState = rootState;
  const {settings: {settings: {selectionId}, id}} = ownProps;
  const userSelection = selectionId && userSelections.entities[selectionId];

  const parameters = userSelection
    ? getMeterParameters({
      userSelection: {
        ...userSelection,
        selectionParameters: {
          ...userSelection.selectionParameters,
        },
      },
    })
    : makeApiParametersOf({period: Period.now});

  const title = userSelection
    ? userSelection.name
    : firstUpperTranslated('all meters');

  return {
    model: data[id],
    bounds: data[id] && data[id].isSuccessfullyFetched
      ? boundsFromMarkers(data[id].data.entities.meterMapMarkers)
      : undefined,
    error: getErrorCalle(data[id]),
    isFetching: data[id] && data[id].isFetching,
    lowConfidenceText: getMeterLowConfidenceTextInfo(rootState),
    map,
    title,
    viewCenter: map.viewCenter,
    parameters,
    isUserSelectionsSuccessfullyFetched: userSelections.isSuccessfullyFetched,
    markers: data[id] && data[id].isSuccessfullyFetched ? data[id].data : {entities: {}, result: []},
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  clearError: clearErrorMeterMapMarkers,
  closeClusterDialog,
  fetchMapWidget,
  deleteWidget,
}, dispatch);

export const MapWidgetContainer =
  connect<StateToProps, DispatchToProps>(() => mapStateToProps, mapDispatchToProps)(MapWidget);

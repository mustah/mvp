import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {Period} from '../../../components/dates/dateModels';
import {MeterDetailsDialog} from '../../../components/dialog/DetailsDialog';
import {withEmptyContent, WithEmptyContentProps} from '../../../components/hoc/withEmptyContent';
import {Column} from '../../../components/layouts/column/Column';
import {RetryLoader} from '../../../components/loading/Loader';
import {Maybe} from '../../../helpers/Maybe';
import {makeApiParametersOf} from '../../../helpers/urlFactory';
import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated} from '../../../services/translationService';
import {DomainModel, RequestsHttp} from '../../../state/domain-models/domainModels';
import {getErrorCalle} from '../../../state/domain-models/domainModelsSelectors';
import {GeoPosition} from '../../../state/domain-models/location/locationModels';
import {deleteWidget} from '../../../state/domain-models/widget/widgetActions';
import {MapWidget, WidgetMandatory} from '../../../state/domain-models/widget/widgetModels';
import {getMeterParameters} from '../../../state/user-selection/userSelectionSelectors';
import {fetchMapWidget, WidgetRequestParameters} from '../../../state/widget/widgetActions';
import {WidgetData} from '../../../state/widget/widgetReducer';
import {CallbackWith, ClearError, ErrorResponse, OnClick, uuid} from '../../../types/Types';
import {Map} from '../../map/components/Map';
import {ClusterContainer} from '../../map/containers/ClusterContainer';
import {boundsFromMarkers} from '../../map/helper/mapHelper';
import {closeClusterDialog} from '../../map/mapActions';
import {clearErrorMeterMapMarkers} from '../../map/mapMarkerActions';
import {Bounds, MapMarker} from '../../map/mapModels';
import {MapState} from '../../map/mapReducer';
import {WidgetWithTitle} from '../components/widgets/Widget';

interface OwnProps {
  height: number;
  width: number;
  widget: MapWidget;
  openConfiguration: OnClick;
  onDelete: CallbackWith<WidgetMandatory>;
}

interface MapContentProps {
  bounds?: Bounds;
  informationText?: string;
  viewCenter?: GeoPosition;
  markers: DomainModel<MapMarker>;
  height?: number;
  width?: number;
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
  fetchMapWidget: CallbackWith<WidgetRequestParameters>;
}

type MapContentWrapperProps = MapContentProps & WithEmptyContentProps;

type Props = MapContentProps & OwnProps & StateToProps & DispatchToProps;

const MapContent = ({bounds, viewCenter, informationText, markers: {entities}, height, width}: MapContentProps) => (
  <Map
    bounds={bounds}
    lowConfidenceText={informationText}
    viewCenter={viewCenter}
    height={height}
    width={width}
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
    informationText,
    map,
    title,
    viewCenter,
    isUserSelectionsSuccessfullyFetched,
    fetchMapWidget,
    openConfiguration,
    widget,
    parameters,
    onDelete,
    width,
    height,
  } = props;

  React.useEffect(() => {
    if (isUserSelectionsSuccessfullyFetched) {
      fetchMapWidget(props);
    }
  }, [widget, parameters, isUserSelectionsSuccessfullyFetched]);

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
    informationText,
    markers,
    hasContent: markers.result.length > 0,
    noContentText: firstUpperTranslated('no meters'),
    viewCenter,
    height,
    width,
  };

  const onClickDeleteWidget = () => onDelete(widget);

  return (
    <WidgetWithTitle
      title={title}
      configure={openConfiguration}
      containerStyle={{paddingBottom: 0}}
      deleteWidget={onClickDeleteWidget}
    >
      <Column style={{width, height}}>
        <RetryLoader isFetching={isFetching} error={error} clearError={clearError}>
          <MapContentWrapper {...wrapperProps}/>
        </RetryLoader>
        {dialog}
      </Column>
    </WidgetWithTitle>
  );
};

const getInformationText = ({data: {result}}: WidgetData): string =>
  firstUpperTranslated('showing {{count}} meters', {count: result.length});

const mapStateToProps = (
  {
    map,
    domainModels: {userSelections},
    widget
  }: RootState,
  {widget: {settings: {selectionId}, id}}: OwnProps
): StateToProps => {
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
    model: widget[id],
    bounds: widget[id] && widget[id].isSuccessfullyFetched
      ? boundsFromMarkers(widget[id].data.entities.meterMapMarkers)
      : undefined,
    error: getErrorCalle(widget[id]),
    isFetching: widget[id] && widget[id].isFetching,
    informationText: widget[id] && widget[id].isSuccessfullyFetched
      ? getInformationText(widget[id]) : undefined,
    map,
    title,
    viewCenter: map.viewCenter,
    parameters,
    isUserSelectionsSuccessfullyFetched: userSelections.isSuccessfullyFetched,
    markers: widget[id] && widget[id].isSuccessfullyFetched ? widget[id].data : {entities: {}, result: []},
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  clearError: clearErrorMeterMapMarkers,
  closeClusterDialog,
  fetchMapWidget,
  deleteWidget,
}, dispatch);

export const MapWidgetContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(() => mapStateToProps, mapDispatchToProps)(MapWidget);

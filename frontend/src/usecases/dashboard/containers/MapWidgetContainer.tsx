import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {Period} from '../../../components/dates/dateModels';
import {withEmptyContent, WithEmptyContentProps} from '../../../components/hoc/withEmptyContent';
import {Column} from '../../../components/layouts/column/Column';
import {RetryLoader} from '../../../components/loading/Loader';
import {Maybe} from '../../../helpers/Maybe';
import {makeApiParametersOf} from '../../../helpers/urlFactory';
import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated} from '../../../services/translationService';
import {RequestsHttp} from '../../../state/domain-models/domainModels';
import {getError} from '../../../state/domain-models/domainModelsSelectors';
import {deleteWidget} from '../../../state/domain-models/widget/widgetActions';
import {MapWidget} from '../../../state/domain-models/widget/widgetModels';
import {getMeterParameters} from '../../../state/user-selection/userSelectionSelectors';
import {fetchMapWidget, WidgetRequestParameters} from '../../../state/widget/widgetActions';
import {WidgetData} from '../../../state/widget/widgetReducer';
import {CallbackWith, ClearError, ErrorResponse, HasContent} from '../../../types/Types';
import {MapMarkerCluster} from '../../map/components/Map';
import {emptyClusters} from '../../map/helper/clusterHelper';
import {boundsFromMarkers} from '../../map/helper/mapHelper';
import {onCenterMap} from '../../map/mapActions';
import {clearErrorMeterMapMarkers} from '../../map/mapMarkerActions';
import {MapComponentProps, MapMarkersProps, MapProps, OnCenterMapEvent} from '../../map/mapModels';
import {getMapZoomSettings, getWidgetMapMarkers} from '../../map/mapSelectors';
import {WidgetWithTitle} from '../components/Widget';
import {WidgetDispatchers} from '../dashboardModels';

interface OwnProps extends WidgetDispatchers {
  height: number;
  width: number;
  widget: MapWidget;
}

interface StateToProps extends MapComponentProps, HasContent, MapMarkersProps {
  model: WidgetData & RequestsHttp;
  error: Maybe<ErrorResponse>;
  isFetching: boolean;
  title: string;
  isUserSelectionsSuccessfullyFetched: boolean;
  parameters: string;
}

interface DispatchToProps extends OnCenterMapEvent {
  clearError: ClearError;
  fetchMapWidget: CallbackWith<WidgetRequestParameters>;
}

type MapContentWrapperProps = MapProps & WithEmptyContentProps;

type Props = MapComponentProps & OwnProps & StateToProps & DispatchToProps;

const MapContentWrapper = withEmptyContent<MapContentWrapperProps>(MapMarkerCluster);

const MapWidget = ({
  bounds,
  center,
  clearError,
  error,
  fetchMapWidget,
  hasContent,
  height,
  id,
  isFetching,
  isUserSelectionsSuccessfullyFetched,
  lowConfidenceText,
  mapMarkerClusters,
  onEdit,
  onDelete,
  onCenterMap,
  parameters,
  title,
  widget,
  width: calculatedWidth,
  zoom,
}: Props) => {
  React.useEffect(() => {
    if (isUserSelectionsSuccessfullyFetched) {
      fetchMapWidget({parameters, widget});
    }
  }, [widget, parameters, isUserSelectionsSuccessfullyFetched]);

  const width = calculatedWidth - 5;

  const wrapperProps: MapContentWrapperProps = {
    bounds,
    center,
    hasContent,
    height,
    id,
    lowConfidenceText,
    mapMarkerClusters,
    noContentText: firstUpperTranslated('no meters'),
    onCenterMap,
    width,
    zoom,
  };

  const onDeleteWidget = () => onDelete(widget);
  const onEditWidget = () => onEdit(widget);

  return (
    <WidgetWithTitle
      containerStyle={{paddingBottom: 0}}
      deleteWidget={onDeleteWidget}
      editWidget={onEditWidget}
      headerClassName="map"
      title={title}
    >
      <Column style={{width, height}} className="MapWrapper">
        <RetryLoader isFetching={isFetching} error={error} clearError={clearError}>
          <MapContentWrapper {...wrapperProps}/>
        </RetryLoader>
      </Column>
    </WidgetWithTitle>
  );
};

const getInformationText = ({data: {result}}: WidgetData): string =>
  firstUpperTranslated('showing {{count}} meters', {count: result.length});

const mapStateToProps = (
  {
    domainModels: {userSelections},
    map,
    widget: widgetState
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

  const widget = widgetState[id];
  const hasContent = widget && widget.isSuccessfullyFetched;

  return {
    bounds: hasContent
      ? boundsFromMarkers(widget.data.entities.meterMapMarkers)
      : undefined,
    error: Maybe.maybe(widget).flatMap(getError),
    id,
    isFetching: widget && widget.isFetching,
    isUserSelectionsSuccessfullyFetched: userSelections.isSuccessfullyFetched,
    hasContent,
    lowConfidenceText: hasContent ? getInformationText(widget) : undefined,
    mapMarkerClusters: hasContent ? getWidgetMapMarkers(widget.data) : emptyClusters,
    model: widget,
    parameters,
    title,
    ...getMapZoomSettings(id)(map),
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  clearError: clearErrorMeterMapMarkers,
  fetchMapWidget,
  deleteWidget,
  onCenterMap,
}, dispatch);

export const MapWidgetContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(() => mapStateToProps, mapDispatchToProps)(MapWidget);

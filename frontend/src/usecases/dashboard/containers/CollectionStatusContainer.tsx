import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {routes} from '../../../app/routes';
import {withWidgetLoader} from '../../../components/hoc/withLoaders';
import {IndicatorWidget, IndicatorWidgetProps} from '../../../components/indicators/IndicatorWidget';
import {thresholdClassName} from '../../../helpers/thresholds';
import {makeApiParametersOf} from '../../../helpers/urlFactory';
import {history} from '../../../index';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {collectionStatClearError} from '../../../state/domain-models/collection-stat/collectionStatActions';
import {RequestsHttp} from '../../../state/domain-models/domainModels';
import {deleteWidget} from '../../../state/domain-models/widget/widgetActions';
import {
  CollectionStatusWidget,
  CountableWidgetModel,
  WidgetMandatory
} from '../../../state/domain-models/widget/widgetModels';
import {resetSelection, selectSavedSelection} from '../../../state/user-selection/userSelectionActions';
import {getCollectionStatParameters} from '../../../state/user-selection/userSelectionSelectors';
import {fetchCollectionStatsWidget, WidgetRequestParameters} from '../../../state/widget/widgetActions';
import {WidgetData} from '../../../state/widget/widgetReducer';
import {Callback, CallbackWith, CallbackWithId, EncodedUriParameters, OnClick} from '../../../types/Types';
import {WidgetWithTitle} from '../components/widgets/Widget';

interface WidgetProps {
  widget: CountableWidgetModel;
  title: string;
  openConfiguration: OnClick;
  deleteWidget: Callback;
  onClickWidget: OnClick;
  isFetching: boolean;
}

const LoadingIndicator = withWidgetLoader<IndicatorWidgetProps>(IndicatorWidget);

const IndicatorContent = ({widget, title, openConfiguration, deleteWidget, onClickWidget, isFetching}: WidgetProps) => (
  <WidgetWithTitle
    title={title}
    configure={openConfiguration}
    deleteWidget={deleteWidget}
    headerClassName={isNaN(widget.count) ? 'info' : thresholdClassName(widget.count)}
  >
    <div onClick={onClickWidget} className="clickable">
      <LoadingIndicator isFetching={isFetching} widget={widget} title={translate('collection')}/>
    </div>
  </WidgetWithTitle>
);

type Props = StateToProps & DispatchToProps & OwnProps;

const CollectionStatus = (props: Props) => {
  const {
    isUserSelectionsSuccessfullyFetched,
    fetchCollectionStatsWidget,
    widget,
    parameters,
    model,
    isUserSelectionsFetching,
    title,
    openConfiguration,
    onDelete,
    selectSavedSelection,
    resetSelection,
  } = props;

  React.useEffect(() => {
    if (isUserSelectionsSuccessfullyFetched) {
      fetchCollectionStatsWidget(props);
    }
  }, [widget, parameters, isUserSelectionsSuccessfullyFetched]);

  const widgetModel: CountableWidgetModel = {count: model && model.data};
  const isFetching = model && model.isFetching || isUserSelectionsFetching;

  const onClickDeleteWidget = () => onDelete(widget);

  const onClickWidget = () => {
    if (widget.settings.selectionId) {
      selectSavedSelection(widget.settings.selectionId);
    } else {
      resetSelection();
    }
    history.push(routes.meter);
  };

  return (
    <IndicatorContent
      isFetching={isFetching}
      widget={widgetModel}
      title={title}
      deleteWidget={onClickDeleteWidget}
      openConfiguration={openConfiguration}
      onClickWidget={onClickWidget}
    />
  );
};

interface OwnProps {
  widget: CollectionStatusWidget;
  openConfiguration: OnClick;
  onDelete: CallbackWith<WidgetMandatory>;
}

interface StateToProps {
  model: WidgetData & RequestsHttp;
  parameters: EncodedUriParameters;
  isUserSelectionsSuccessfullyFetched: boolean;
  isUserSelectionsFetching: boolean;
  title: string;
}

interface DispatchToProps {
  fetchCollectionStatsWidget: CallbackWith<WidgetRequestParameters>;
  selectSavedSelection: CallbackWithId;
  resetSelection: Callback;
}

const mapStateToProps = (
  {domainModels: {userSelections}, widget}: RootState,
  {widget: {settings: {selectionInterval, selectionId}, id}}: OwnProps
): StateToProps => {
  const userSelection = selectionId && userSelections.entities[selectionId];

  // TODO: fix
  const parameters =
    userSelection
      ? makeApiParametersOf(selectionInterval) + '&' + getCollectionStatParameters({
      userSelection: {
        ...userSelection,
        selectionParameters: {
          ...userSelection.selectionParameters,
        },
      },
    })
      : makeApiParametersOf(selectionInterval);

  const title = userSelection
    ? userSelection.name
    : translate('all meters');

  return {
    model: widget[id],
    parameters,
    isUserSelectionsSuccessfullyFetched: userSelections.isSuccessfullyFetched,
    isUserSelectionsFetching: userSelections.isFetching,
    title,
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  clearError: collectionStatClearError, // TODO add id here
  fetchCollectionStatsWidget,
  selectSavedSelection,
  resetSelection,
  deleteWidget,
}, dispatch);

export const CollectionStatusContainer = connect<StateToProps, DispatchToProps, OwnProps>(
  mapStateToProps,
  mapDispatchToProps
)(CollectionStatus);

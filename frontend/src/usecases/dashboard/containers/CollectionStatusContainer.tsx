import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {routes} from '../../../app/routes';
import {withWidgetLoader} from '../../../components/hoc/withLoaders';
import {IndicatorWidget, IndicatorWidgetProps} from '../../../components/indicators/IndicatorWidget';
import {WidgetModel} from '../../../components/indicators/indicatorWidgetModels';
import {Column, ColumnCenter} from '../../../components/layouts/column/Column';
import {Row} from '../../../components/layouts/row/Row';
import {makeApiParametersOf} from '../../../helpers/urlFactory';
import {history} from '../../../index';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {collectionStatClearError, } from '../../../state/domain-models/collection-stat/collectionStatActions';
import {RequestsHttp} from '../../../state/domain-models/domainModels';
import {deleteWidget} from '../../../state/domain-models/widget/widgetActions';
import {resetSelection, selectSavedSelection} from '../../../state/user-selection/userSelectionActions';
import {SelectionInterval} from '../../../state/user-selection/userSelectionModels';
import {getCollectionStatParameters} from '../../../state/user-selection/userSelectionSelectors';
import {WidgetMandatory, WidgetType} from '../../../state/widget/configuration/widgetConfigurationReducer';
import {fetchCollectionStatsWidget, FetchWidgetIfNeeded} from '../../../state/widget/data/widgetDataActions';
import {WidgetData} from '../../../state/widget/data/widgetDataReducer';
import {Callback, CallbackWith, CallbackWithId, EncodedUriParameters, OnClick, uuid} from '../../../types/Types';
import '../components/widgets/CollectionStatus.scss';
import {WidgetWithTitle} from '../components/widgets/Widget';

interface WidgetProps {
  widget: WidgetModel;
  title: string;
  openConfiguration: OnClick;
  deleteWidget: Callback;
  onClickWidget: OnClick;
  isFetching: boolean;
}

const IndicatorContent = ({widget, title, openConfiguration, deleteWidget, onClickWidget, isFetching}: WidgetProps) =>
  (
    <Row>
      <WidgetWithTitle
        title={title}
        configure={openConfiguration}
        deleteWidget={deleteWidget}
      > <div onClick={onClickWidget} className={'widget-link'}>
          <LoadingIndicator isFetching={isFetching} widget={widget} title={translate('collection')}/>
      </div>
      </WidgetWithTitle>
    </Row>
  );

const LoadingIndicator = withWidgetLoader<IndicatorWidgetProps>(IndicatorWidget);

export interface CollectionStatusWidgetSettings extends WidgetMandatory {
  type: WidgetType.COLLECTION;
  settings: {
    selectionId?: uuid;
    selectionInterval: SelectionInterval;
  };
}

type Props = StateToProps & DispatchToProps & OwnProps;

const CollectionStatus = (props: Props) => {
  const {
    isUserSelectionsSuccessfullyFetched,
    fetchCollectionStatsWidget,
    settings,
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
  }, [settings, parameters, isUserSelectionsSuccessfullyFetched]);

  const widget: WidgetModel = {
    collectionPercentage: model && model.data
  };
  const isFetching = model && model.isFetching || isUserSelectionsFetching;

  const onClickDeleteWidget = () => onDelete(settings);

  const onClickWidget = () => {
    if (settings.settings.selectionId) {
      selectSavedSelection(settings.settings.selectionId);
    } else {
      resetSelection();
    }
    history.push(routes.meter);
  };

  return (
    <Column className="CollectionStatus">
        <ColumnCenter className="flex-1">
          <IndicatorContent
            isFetching={isFetching}
            widget={widget}
            title={title}
            deleteWidget={onClickDeleteWidget}
            openConfiguration={openConfiguration}
            onClickWidget={onClickWidget}
          />
        </ColumnCenter>
    </Column>
  );
};

interface OwnProps {
  settings: CollectionStatusWidgetSettings;
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
  fetchCollectionStatsWidget: CallbackWith<FetchWidgetIfNeeded>;
  selectSavedSelection: CallbackWithId;
  resetSelection: Callback;
}

const mapStateToProps = (
  {domainModels: {userSelections}, widget: {data}}: RootState,
  {settings: {settings: {selectionInterval, selectionId}, id}}: OwnProps
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
    model: data[id],
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

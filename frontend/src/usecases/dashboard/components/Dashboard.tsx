import {isEqual} from 'lodash';
import * as React from 'react';
import ReactGridLayout, {Layout} from 'react-grid-layout';
import 'react-grid-layout/css/styles.css';
import 'react-resizable/css/styles.css';
import {ActionMenuItem} from '../../../components/actions-dropdown/ActionMenuItem';
import {EmptyContent} from '../../../components/error-message/EmptyContent';
import {WithEmptyContentProps} from '../../../components/hoc/withEmptyContent';
import {withLargeLoader} from '../../../components/hoc/withLoaders';
import {isDefined} from '../../../helpers/commonHelpers';
import {Maybe} from '../../../helpers/Maybe';
import {firstUpperTranslated, translate} from '../../../services/translationService';
import {
  CollectionStatusWidget,
  MapWidget,
  Widget,
  WidgetMandatory,
  WidgetType
} from '../../../state/domain-models/widget/widgetModels';
import {widgetHeightToPx, widgetMargins, widgetWidthToPx} from '../../../state/widget/widgetConfiguration';
import {Dictionary, OnClick, RenderFunction} from '../../../types/Types';
import {CollectionStatusWidgetContainer} from '../containers/CollectionStatusWidgetContainer';
import {CountWidgetContainer} from '../containers/CountWidgetContainer';
import {DispatchToProps, StateToProps} from '../containers/DashboardContainer';
import {EditCollectionStatusWidgetContainer} from '../containers/EditCollectionStatusWidgetContainer';
import {EditWidgetContainer} from '../containers/EditWidgetContainer';
import {MapWidgetContainer} from '../containers/MapWidgetContainer';
import {addToNextRow, makeDefaultWidget} from '../dashboardHelpers';
import {WidgetDispatchers} from '../dashboardModels';
import {AddWidgetButton} from './AddWidgetButton';
import './Widget.scss';

type ElementFromWidgetType = (widgets: Widget['type']) => any;

const newWidgetMenu =
  (openDialogWithWidgetType: ElementFromWidgetType): RenderFunction<OnClick> =>
    (closeMenu: OnClick) => {

      const selectMenuItem = (type: WidgetType) => () => {
        closeMenu();
        openDialogWithWidgetType(type);
      };

      return [
        (
          <ActionMenuItem
            name={translate('map')}
            onClick={selectMenuItem(WidgetType.MAP)}
            key="map"
          />
        ),
        (
          <ActionMenuItem
            name={translate('collection status')}
            onClick={selectMenuItem(WidgetType.COLLECTION)}
            key="collectionStatus"
          />
        ),
        (
          <ActionMenuItem
            name={translate('meter count')}
            onClick={selectMenuItem(WidgetType.COUNT)}
            key="meterCount"
          />
        ),
      ];
    };

const makeLayoutComparable = ({h, w, x, y}: Layout): Layout => ({h, w, x, y});

const gridStyle: React.CSSProperties = {
  top: -24,
  left: -24,
};

const renderWidget = (
  widget: Widget,
  width: number,
  height: number,
  widgetDispatchers: WidgetDispatchers,
) => {
  if (widget.type === WidgetType.MAP) {
    return (
      <MapWidgetContainer
        width={widgetWidthToPx(width)}
        height={widgetHeightToPx(height)}
        widget={widget}
        {...widgetDispatchers}
      />);
  } else if (widget.type === WidgetType.COLLECTION) {
    return <CollectionStatusWidgetContainer widget={widget} {...widgetDispatchers}/>;
  } else if (widget.type === WidgetType.COUNT) {
    return <CountWidgetContainer widget={widget} {...widgetDispatchers}/>;
  } else {
    return null;
  }
};

type Props = StateToProps & DispatchToProps;

export const Dashboard = ({
  addWidgetToDashboard,
  dashboard: existingDashboard,
  deleteWidget,
  hasContent,
  noContentText,
  updateWidget,
  updateDashboard,
  widgets,
}: Props & WithEmptyContentProps) => {
  const [currentWidget, editWidget] = React.useState<Maybe<Widget>>(Maybe.nothing());
  const dashboard = existingDashboard.get();

  const closeConfigurationDialog = () => editWidget(Maybe.nothing());
  const onEdit = (widget: Widget) => editWidget(Maybe.just(widget));
  const onDelete = (widget: WidgetMandatory) => deleteWidget(widget.id);

  const onLayoutChange = (layout: Layout[]) => {
    if (!isEqual(
      dashboard.layout.layout.map(makeLayoutComparable).sort(),
      layout.map(makeLayoutComparable).sort()
    )) {
      updateDashboard({...dashboard, layout: {layout}});
    }
  };

  const widgetsSettings: Dictionary<Widget> = widgets.entities;

  const layout: Layout[] = dashboard.layout.layout
    .filter(layout => isDefined(widgetsSettings[layout.i as string]))
    .map((layout: Layout) => ({
      ...layout,
      isDraggable: true,
      isResizable: widgetsSettings[layout.i!].type === WidgetType.MAP,
    }));

  const saveWidgetConfiguration = (widget: WidgetMandatory) => {
    if (widgets.result.find(id => id === widget.id) === undefined) {
      updateDashboard({...dashboard, layout: {layout: addToNextRow(widget, layout)}});
      addWidgetToDashboard(widget);
    } else {
      updateWidget(widget);
    }
    closeConfigurationDialog();
  };

  const widgetDispatchers: WidgetDispatchers = {onDelete, onEdit};

  const widgetComponents = layout.map(({i, w, h}) => (
    <div key={i}>
      {renderWidget(widgetsSettings[i as string], w, h, widgetDispatchers)}
    </div>
  ));

  const editCollectionPercentageWidgetDialog = currentWidget
    .filter(({type}) => type === WidgetType.COLLECTION)
    .map(settings => (
      <EditCollectionStatusWidgetContainer
        id={settings.id}
        settings={settings as CollectionStatusWidget}
        dashboardId={dashboard.id}
        isOpen={true}
        onCancel={closeConfigurationDialog}
        onSave={saveWidgetConfiguration}
      />))
    .getOrElseNull();

  const editWidgetDialog = currentWidget
    .filter(({type}) => type === WidgetType.MAP || type === WidgetType.COUNT)
    .map(settings => (
      <EditWidgetContainer
        id={settings.id}
        widgets={settings as MapWidget}
        dashboardId={dashboard.id}
        isOpen={true}
        onCancel={closeConfigurationDialog}
        onSave={saveWidgetConfiguration}
      />))
    .getOrElseNull();

  const renderPopoverContent =
    newWidgetMenu((type: WidgetType) => editWidget(Maybe.just(makeDefaultWidget(dashboard.id, type))));

  return (
    <>
      <AddWidgetButton renderPopoverContent={renderPopoverContent}/>

      {editCollectionPercentageWidgetDialog}
      {editWidgetDialog}

      <ReactGridLayout
        layout={layout}
        width={1200}
        cols={6}
        rowHeight={170}
        onLayoutChange={onLayoutChange}
        draggableHandle=".grid-draggable"
        draggableCancel=".grid-not-draggable"
        margin={widgetMargins}
        style={gridStyle}
      >
        {widgetComponents}
      </ReactGridLayout>

      {!hasContent && <EmptyContent noContentText={noContentText}/>}
    </>
  );
};

const LoadingDashboard = withLargeLoader<Props & WithEmptyContentProps>(Dashboard);

export const DashboardComponent = (props: Props) => {
  React.useEffect(() => {
    props.onFetchDashboards();
  }, [props.isFetching, props.isSuccessfullyFetched, props.widgets.isSuccessfullyFetched]);

  return <LoadingDashboard {...props} noContentText={firstUpperTranslated('dashboard is empty')}/>;
};

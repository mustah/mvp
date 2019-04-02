import {isEqual} from 'lodash';
import * as React from 'react';
import ReactGridLayout, {Layout} from 'react-grid-layout';
import 'react-grid-layout/css/styles.css';
import 'react-resizable/css/styles.css';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history3/redirect';
import {ActionMenuItem} from '../../../components/actions-dropdown/ActionMenuItem';
import {Period} from '../../../components/dates/dateModels';
import {PageLayout} from '../../../components/layouts/layout/PageLayout';
import {Row} from '../../../components/layouts/row/Row';
import {MainTitle} from '../../../components/texts/Titles';
import {isDefined} from '../../../helpers/commonHelpers';
import {idGenerator} from '../../../helpers/idGenerator';
import {Maybe} from '../../../helpers/Maybe';
import {translate} from '../../../services/translationService';
import {Dashboard as DashboardModel} from '../../../state/domain-models/dashboard/dashboardModels';
import {NormalizedState} from '../../../state/domain-models/domainModels';
import {
  CollectionStatusWidget,
  MapWidget,
  Widget,
  WidgetMandatory,
  WidgetType
} from '../../../state/domain-models/widget/widgetModels';
import {widgetHeightToPx, widgetMargins, widgetWidthToPx} from '../../../state/widget/widgetConfiguration';
import {OnClick, RenderFunction, uuid} from '../../../types/Types';
import {CollectionStatusWidgetContainer} from '../containers/CollectionStatusWidgetContainer';
import {CountWidgetContainer} from '../containers/CountWidgetContainer';
import {DispatchToProps, StateToProps} from '../containers/DashboardContainer';
import {EditCollectionStatusWidgetContainer} from '../containers/EditCollectionStatusWidgetContainer';
import {EditWidgetContainer} from '../containers/EditWidgetContainer';
import {MapWidgetContainer} from '../containers/MapWidgetContainer';
import {WidgetDispatchers} from '../dashboardModels';
import {AddNewWidgetButton} from './AddNewWidgetButton';
import './Widget.scss';

type ElementFromWidgetType = (widgets: Widget['type']) => any;

const makeLayoutComparable = ({h, w, x, y}: Layout): Layout => ({h, w, x, y});

const gridStyle: React.CSSProperties = {
  top: -24,
  left: -24,
};

const newWidgetMenu =
  (openDialogWithWidgetType: ElementFromWidgetType): RenderFunction<OnClick> =>
    (closeMenu: OnClick) => {

      const selectMenuItem =
        (type: WidgetType) =>
          () => {
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

const hasContent = (
  dashboard?: DashboardModel,
  widgets?: NormalizedState<Widget>
): boolean =>
  dashboard !== undefined
  && dashboard.layout !== undefined
  && widgets !== undefined
  && widgets.isSuccessfullyFetched;

const makeDefaultCollectionWidget = (dashboardId: uuid): CollectionStatusWidget => ({
  id: idGenerator.uuid().toString(),
  dashboardId,
  settings: {
    selectionInterval: {
      period: Period.latest,
    },
  },
  type: WidgetType.COLLECTION,
});

const makeDefaultMapWidget = (dashboardId: uuid): MapWidget => ({
  id: idGenerator.uuid().toString(),
  dashboardId,
  settings: {},
  type: WidgetType.MAP,
});

const makeDefaultDashboard = (id: uuid, mapWidgetId: uuid, collectionWidgetId: uuid): DashboardModel => {
  const collectionProps = widgetSizeMap[WidgetType.COLLECTION];
  const mapProps = widgetSizeMap[WidgetType.MAP];
  return ({
    id,
    layout: {
      layout: [
        {i: collectionWidgetId.toString(), x: mapProps.w + 1, y: 0, w: collectionProps.w, h: collectionProps.h},
        {i: mapWidgetId.toString(), x: 0, y: 0, w: mapProps.w, h: mapProps.h},
      ]
    }
  });
};

interface LayoutProps {
  w: number;
  h: number;
}

const widgetSizeMap: { [w in WidgetType]: LayoutProps } = {
  [WidgetType.MAP]: {w: 5, h: 4},
  [WidgetType.COLLECTION]: {w: 1, h: 1},
  [WidgetType.COUNT]: {w: 1, h: 1},
};

const getDefaultWidgets = (
  map: MapWidget,
  collection: CollectionStatusWidget
): NormalizedState<Widget> => ({
  isSuccessfullyFetched: true,
  isFetching: false,
  result: [collection.id, map.id],
  entities: {
    [map.id]: {...map},
    [collection.id]: {...collection},
  },
  total: 2,
});

const addToNextRow = (widgetSettings: WidgetMandatory, layout: Layout[]): Layout[] =>
  [
    ...layout,
    {
      i: widgetSettings.id.toString(),
      x: 0,
      y: layout.reduce((previous: number, {y, h}: Layout) => Math.max(previous, y + h), -1) + 1,
      w: widgetSizeMap[widgetSettings.type].w,
      h: widgetSizeMap[widgetSettings.type].h,
    },
  ];

const defaultWidget = (dashboardId: uuid, type: WidgetType): Widget => {
  if (type === WidgetType.COLLECTION) {
    return {
      dashboardId,
      id: idGenerator.uuid(),
      type,
      settings: {
        selectionInterval: {
          period: Period.latest,
        },
      },
    };
  } else if (type === WidgetType.MAP) {
    return {
      dashboardId,
      id: idGenerator.uuid(),
      type,
      settings: {},
    };
  } else {
    return {
      dashboardId,
      id: idGenerator.uuid(),
      type: WidgetType.COUNT,
      settings: {},
    };
  }
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

type Props = StateToProps & DispatchToProps & InjectedAuthRouterProps;

export const Dashboard = ({
  addDashboard,
  addWidgetToDashboard,
  dashboard,
  deleteWidget,
  fetchDashboard,
  fetchWidgets,
  isSuccessfullyFetched,
  parameters,
  updateWidget,
  updateDashboard,
  widgets,
}: Props) => {
  React.useEffect(() => {
    fetchDashboard();
    if (isSuccessfullyFetched) {
      fetchWidgets(parameters);
    }
  }, [parameters]);

  const [widgetBeingEdited, editWidget] = React.useState<Maybe<Widget>>(Maybe.nothing());

  let myDashboard: DashboardModel | undefined;
  let myWidgets: NormalizedState<Widget>;

  let dashboardId: uuid = 'TODO hardcoded';

  if (!dashboard && isSuccessfullyFetched) {
    dashboardId = idGenerator.uuid();
    const map = makeDefaultMapWidget(dashboardId);
    const collection = makeDefaultCollectionWidget(dashboardId);

    myDashboard = makeDefaultDashboard(dashboardId, map.id, collection.id);
    myWidgets = getDefaultWidgets(map, collection);

    addDashboard(myDashboard);
  } else {
    myDashboard = dashboard;
    myWidgets = widgets;
  }

  const closeConfigurationDialog = () => editWidget(Maybe.nothing());

  const onEdit = (widget: Widget) => editWidget(Maybe.just(widget));

  const onDelete = (widget: WidgetMandatory) => deleteWidget(widget.id);

  let layout: Layout[] = [];
  const onLayoutChange = (layout: Layout[]) => {
    if (hasContent(myDashboard, myWidgets)
        && !isEqual(
        myDashboard!.layout.layout.map(makeLayoutComparable).sort(),
        layout.map(makeLayoutComparable).sort()
      )) {
      updateDashboard({...myDashboard, layout: {layout}});
    }
  };

  const saveWidgetConfiguration = (widget: WidgetMandatory) => {
    if (myWidgets.result.find(id => id === widget.id) === undefined) {
      addWidgetToDashboard(widget);
      updateDashboard({...myDashboard, layout: {layout: addToNextRow(widget, layout)}});
    } else {
      updateWidget(widget);
    }
    closeConfigurationDialog();
  };

  // TODO handle empty dashboard
  if (myDashboard) {
    dashboardId = myDashboard.id;
  }

  // TODO trigger fetching this when layout is non-empty (and widgetsettings is empty)
  let widgetsWithSettings: {[key: string]: Widget} = {};

  // TODO handle empty
  if (hasContent(myDashboard, myWidgets)) {
    widgetsWithSettings = myWidgets.entities;
  }

  if (hasContent(myDashboard, myWidgets)) {
    layout = myDashboard!.layout.layout
      .filter(layout => isDefined(widgetsWithSettings[layout.i as string]))
      .map((layout: Layout) => ({
        ...layout,
        isDraggable: true,
        isResizable: widgetsWithSettings[layout.i as string].type === WidgetType.MAP,
      }));
  }

  // TODO handle empty
  // TODO filter widget that do not exists in both 'layout' and 'myWidgets'
  let widgetsA;
  if (hasContent(myDashboard, myWidgets)) {
    const widgetDispatchers: WidgetDispatchers = {onDelete, onEdit};
    widgetsA = layout.map(({i, w, h}) => (
      <div key={i}>
        {renderWidget(widgetsWithSettings[i as string], w, h, widgetDispatchers)}
      </div>
    ));
  }

  const editCollectionPercentageWidgetDialog = widgetBeingEdited
    .filter(({type}) => type === WidgetType.COLLECTION)
    .map(settings => (
      <EditCollectionStatusWidgetContainer
        id={settings.id}
        settings={settings as CollectionStatusWidget}
        dashboardId={dashboardId}
        isOpen={true}
        onCancel={closeConfigurationDialog}
        onSave={saveWidgetConfiguration}
      />))
    .getOrElseNull();

  const editWidgetDialog = widgetBeingEdited
    .filter(({type}) => type === WidgetType.MAP || type === WidgetType.COUNT)
    .map(settings => (
      <EditWidgetContainer
        id={settings.id}
        widgets={settings as MapWidget}
        dashboardId={dashboardId}
        isOpen={true}
        onCancel={closeConfigurationDialog}
        onSave={saveWidgetConfiguration}
      />))
    .getOrElseNull();

  const onAddNewWidget = newWidgetMenu((type: WidgetType) => editWidget(Maybe.just(defaultWidget(dashboardId, type))));

  return (
    <PageLayout>
      <Row className="space-between">
        <MainTitle>{translate('dashboard')}</MainTitle>
        <AddNewWidgetButton renderPopoverContent={onAddNewWidget}/>
      </Row>

      <ReactGridLayout
        layout={layout}
        width={1200}
        cols={6}
        rowHeight={170}
        onLayoutChange={onLayoutChange}
        draggableHandle={'.grid-draggable'}
        margin={widgetMargins}
        draggableCancel={'.grid-not-draggable'}
        style={gridStyle}
      >
        {widgetsA}
      </ReactGridLayout>

      {editCollectionPercentageWidgetDialog}
      {editWidgetDialog}
    </PageLayout>
  );
};

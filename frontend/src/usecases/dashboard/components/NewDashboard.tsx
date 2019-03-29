import {isEqual} from 'lodash';
import * as React from 'react';
import ReactGridLayout, {Layout} from 'react-grid-layout';
import 'react-grid-layout/css/styles.css';
import 'react-resizable/css/styles.css';
import {ActionMenuItem} from '../../../components/actions-dropdown/ActionMenuItem';
import {Period} from '../../../components/dates/dateModels';
import {PageLayout} from '../../../components/layouts/layout/PageLayout';
import {Row} from '../../../components/layouts/row/Row';
import {MainTitle} from '../../../components/texts/Titles';
import {idGenerator} from '../../../helpers/idGenerator';
import {Maybe} from '../../../helpers/Maybe';
import {translate} from '../../../services/translationService';
import {Dashboard} from '../../../state/domain-models/dashboard/dashboardModels';
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
import {CollectionStatusContainer} from '../containers/CollectionStatusContainer';
import {CountWidgetContainer} from '../containers/CountWidgetContainer';
import {EditCollectionStatusWidgetContainer} from '../containers/EditCollectionStatusWidgetContainer';
import {EditWidgetContainer} from '../containers/EditWidgetContainer';
import {MapWidgetContainer} from '../containers/MapWidgetContainer';
import {DashboardProps} from '../dashboardEnhancers';
import {AddNewWidgetButton} from './AddNewWidgetButton';
import './widgets/Widget.scss';

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
  isDashboardFetching: boolean,
  dashboard?: Dashboard,
  widgets?: NormalizedState<Widget>
): boolean =>
  dashboard !== undefined
  && !isDashboardFetching
  && dashboard.layout !== undefined
  && widgets !== undefined
  && widgets.isSuccessfullyFetched
  && !widgets.isFetching;

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

const makeDefaultDashboard = (id: uuid, mapWidgetId: uuid, collectionWidgetId: uuid): Dashboard => {
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

const removeWidget = ({id}: WidgetMandatory, layout: Layout[]): Layout[] => layout.filter(it => it.i !== id);

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

export const NewDashboard = (props: DashboardProps) => {
  const {
    dashboard,
    isFetching,
    isSuccessfullyFetched,
    widgets,
    addWidgetToDashboard,
    updateWidget,
    updateDashboard,
    addDashboard,
    deleteWidget,
  } = props;

  let myDashboard;
  let myWidgets;

  let dashboardId: uuid = 'TODO hardcoded';

  if (!dashboard && isSuccessfullyFetched && !isFetching) {
    dashboardId = idGenerator.uuid();
    const map = makeDefaultMapWidget(dashboardId);
    const collection = makeDefaultCollectionWidget(dashboardId);

    myDashboard = makeDefaultDashboard(dashboardId, map.id, collection.id);
    myWidgets = getDefaultWidgets(map, collection);

    myDashboard.widgets = [map, collection];

    addDashboard(myDashboard);
  } else {
    myDashboard = dashboard;
    myWidgets = widgets;
  }

  const [widgetBeingEdited, editWidget] = React.useState<Maybe<Widget>>(Maybe.nothing());

  const closeConfigurationDialog = () => editWidget(Maybe.nothing());

  const showConfigurationDialog = (settings: Widget) => () => editWidget(Maybe.just(settings));

  const deleteWidgetConfiguration = (widgetSettings: WidgetMandatory) => {
    const newLayout: Layout[] = removeWidget(widgetSettings, layout);
    updateDashboard({...myDashboard, layout: {layout: newLayout}});
    deleteWidget(widgetSettings.id);
  };

  const renderWidget =
    (
      dashboardId: uuid,
      widgets: Widget,
      openConfiguration: (settings: Widget) => OnClick,
      width: number,
      height: number
    ) => {
      if (widgets.type === WidgetType.MAP) {
        return (
          <MapWidgetContainer
            width={widgetWidthToPx(width)}
            height={widgetHeightToPx(height)}
            widget={widgets}
            onDelete={deleteWidgetConfiguration}
            openConfiguration={openConfiguration(widgets)}
          />
        );
      }

      if (widgets.type === WidgetType.COLLECTION) {
        return (
          <CollectionStatusContainer
            widget={widgets}
            onDelete={deleteWidgetConfiguration}
            openConfiguration={openConfiguration(widgets)}
          />
        );
      }

      if (widgets.type === WidgetType.COUNT) {
        return (
          <CountWidgetContainer
            widget={widgets}
            onDelete={deleteWidgetConfiguration}
            openConfiguration={openConfiguration(widgets)}
          />
        );
      }

      return null;
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

  let layout: Layout[] = [];
  const onLayoutChange = (layouts: Layout[]) => {
    if (hasContent(isFetching, myDashboard, myWidgets)
        && !isEqual(
        myDashboard.layout.layout.map(makeLayoutComparable).sort(),
        layouts.map(makeLayoutComparable).sort()
      )
        && layouts.length > 0) {
      updateDashboard({...myDashboard, layout: {layout: layouts}});
    }
  };

  // TODO handle empty dashboard
  if (myDashboard) {
    dashboardId = myDashboard.id;
  }

  // TODO trigger fetching this when layout is non-empty (and widgetsettings is empty)
  let widgetsWithSettings: {[key: string]: Widget} = {};

  // TODO handle empty
  if (hasContent(isFetching, myDashboard, myWidgets)) {
    widgetsWithSettings = myWidgets.entities;
  }

  if (hasContent(isFetching, myDashboard, myWidgets)) {
    layout = myDashboard.layout.layout.map((widgetLayout: Layout) => ({
      ...widgetLayout,
      isDraggable: true,
      isResizable: widgetsWithSettings[widgetLayout.i as string].type === WidgetType.MAP,
    }));
  }

  // TODO handle empty
  // TODO filter widget that do not exists in both 'layout' and 'myWidgets'
  let widgetsA;
  if (hasContent(isFetching, myDashboard, myWidgets)) {
    widgetsA = layout.map(
      ({i, w, h}) => (
        <div key={i}>
          {renderWidget(dashboardId, widgetsWithSettings[i as string], showConfigurationDialog, w, h)}
        </div>
      )
    );
  }

  const editCollectionPercentageWidgetDialog = widgetBeingEdited
    .filter(({type}) => type === WidgetType.COLLECTION)
    .map(
      settings => (
        <EditCollectionStatusWidgetContainer
          id={settings.id}
          settings={settings as CollectionStatusWidget}
          dashboardId={dashboardId}
          isOpen={true}
          onCancel={closeConfigurationDialog}
          onSave={saveWidgetConfiguration}
        />
      )
    )
    .getOrElseNull();

  const editWidgetDialog = widgetBeingEdited
    .filter(({type}) => type === WidgetType.MAP || type === WidgetType.COUNT)
    .map(
      settings => (
        <EditWidgetContainer
          id={settings.id}
          widgets={settings as MapWidget}
          dashboardId={dashboardId}
          isOpen={true}
          onCancel={closeConfigurationDialog}
          onSave={saveWidgetConfiguration}
        />
      )
    )
    .getOrElseNull();

  const addNewWidget = newWidgetMenu((type: WidgetType) =>
    editWidget(Maybe.just(defaultWidget(dashboardId, type))));

  return (
    <PageLayout>
      <Row className="space-between">
        <MainTitle>{translate('dashboard')}</MainTitle>
        <AddNewWidgetButton renderPopoverContent={addNewWidget}/>
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

import {isEqual} from 'lodash';
import Add from 'material-ui/svg-icons/content/add';
import * as React from 'react';
import ReactGridLayout, {Layout} from 'react-grid-layout';
/* tslint:disable */
import 'react-grid-layout/css/styles.css';
import 'react-resizable/css/styles.css';
/* tslint:enable */
import {colors, iconSizeMedium} from '../../../app/themes';
import {ActionMenuItem} from '../../../components/actions-dropdown/ActionMenuItem';
import {ActionsDropdown} from '../../../components/actions-dropdown/ActionsDropdown';
import {Period} from '../../../components/dates/dateModels';
import {Column} from '../../../components/layouts/column/Column';
import {Row} from '../../../components/layouts/row/Row';
import {MainTitle} from '../../../components/texts/Titles';
import {PageLayout} from '../../../containers/PageLayout';
import {idGenerator} from '../../../helpers/idGenerator';
import {Maybe} from '../../../helpers/Maybe';
import {firstUpperTranslated, translate} from '../../../services/translationService';
import {Dashboard} from '../../../state/domain-models/dashboard/dashboardModels';
import {NormalizedState} from '../../../state/domain-models/domainModels';
import {Widget} from '../../../state/domain-models/widget/WidgetModels';
import {
  widgetHeighToPx,
  WidgetMandatory, widgetMargins,
  WidgetSettings,
  widgetSizeMap,
  WidgetType,
  widgetWidthToPx
} from '../../../state/widget/configuration/widgetConfigurationReducer';
import {OnClick, RenderFunction, uuid} from '../../../types/Types';
import {CollectionStatusContainer, CollectionStatusWidgetSettings} from '../containers/CollectionStatusContainer';
import {EditCollectionStatusWidgetContainer} from '../containers/EditCollectionStatusWidgetContainer';
import {MapWidgetContainer, MapWidgetSettings} from '../containers/MapWidgetContainer';
import {DashboardProps} from '../dashboardEnhancers';
import {EditMapWidgetContainer} from './widgets/EditMapWidget';

type ElementFromWidgetType = (settings: WidgetSettings['type']) => any;

const makeLayoutComparable = (layout: Layout): Layout => {
  const {...comparableProps} = {...layout};
  delete comparableProps.static;
  return comparableProps;
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
      ];
    };

const hasContent = (isDashboardFetching: boolean, dashboard?: Dashboard, widget?: NormalizedState<Widget>): boolean => {
  if (dashboard
      && !isDashboardFetching
      && dashboard.layout
      && dashboard.layout.layout
      && widget
      && widget.isSuccessfullyFetched
      && !widget.isFetching) {
    return true;
  }

  return false;
};

const getDefaultCollectionWidget = (dashboardId: uuid): CollectionStatusWidgetSettings => ({
  id: idGenerator.uuid().toString(),
  dashboardId,
  settings: {
    selectionInterval: {
      period: Period.latest,
    },
  },
  type: WidgetType.COLLECTION,
});

const getDefaultMapWidget = (dashboardId: uuid): MapWidgetSettings => ({
  id: idGenerator.uuid().toString(),
  dashboardId,
  settings: {},
  type: WidgetType.MAP,
});

const getDefaultDashboard = (id: uuid, mapWidgetId: uuid, collectionWidgetId: uuid): Dashboard => {
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

const getDefaultWidgets = (
  map: MapWidgetSettings,
  collection: CollectionStatusWidgetSettings
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

const removeWidget = (widgetSettings: WidgetMandatory, layout: Layout[]): Layout[] =>
  layout.filter((l) => l.i !== widgetSettings.id);

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
    const map = getDefaultMapWidget(dashboardId);
    const collection = getDefaultCollectionWidget(dashboardId);

    myDashboard = getDefaultDashboard(dashboardId, map.id, collection.id);
    myWidgets = getDefaultWidgets(map, collection);

    myDashboard.widgets = [map, collection];

    addDashboard(myDashboard);
  } else {
    myDashboard = dashboard;
    myWidgets = widgets;
  }

  const [widgetBeingEdited, editWidget] = React.useState<Maybe<WidgetSettings>>(Maybe.nothing());

  const closeConfigurationDialog = () => editWidget(Maybe.nothing());

  const showConfigurationDialog =
    (settings: WidgetSettings) =>
      () => editWidget(Maybe.just(settings));

  const deleteWidgetConfiguration = (widgetSettings: WidgetMandatory) => {
    const newLayout: Layout[] = removeWidget(widgetSettings, layout);
    updateDashboard({...myDashboard, layout: {layout: newLayout}});
    deleteWidget(widgetSettings.id);
  };

  const renderWidget =
    (
      dashboardId: uuid,
      settings: WidgetSettings,
      openConfiguration: (settings: WidgetSettings) => OnClick,
      width: number,
      height: number
    ) => {
      if (settings.type === WidgetType.MAP) {
        return (
          <MapWidgetContainer
            width={widgetWidthToPx(width)}
            height={widgetHeighToPx(height)}
            settings={settings}
            onDelete={deleteWidgetConfiguration}
            openConfiguration={openConfiguration(settings)}
          />
        );
      }

      if (settings.type === WidgetType.COLLECTION) {
        return (
          <CollectionStatusContainer
            settings={settings}
            onDelete={deleteWidgetConfiguration}
            openConfiguration={openConfiguration(settings)}
          />
        );
      }

      return null;
    };

  const saveWidgetConfiguration = (widgetSettings: WidgetMandatory) => {
    if (myWidgets.result.find(id => id === widgetSettings.id) === undefined) {
      addWidgetToDashboard(widgetSettings);
      const newLayout: Layout[] = addToNextRow(widgetSettings, layout);

      updateDashboard({...myDashboard, layout: {layout: newLayout}});
    } else {
      updateWidget(widgetSettings);
    }

    closeConfigurationDialog();
  };

  let layout: Layout[] = [];
  const onLayoutChange = (layout: Layout[]) => {
    if (hasContent(isFetching, myDashboard, myWidgets)
        && !isEqual(myDashboard.layout.layout.map(makeLayoutComparable).sort(), layout.map(makeLayoutComparable).sort())
        && layout.length > 0) {
      updateDashboard({...myDashboard, layout: {layout}});
    }
  };

  // TODO handle empty dashboard
  if (myDashboard) {
    dashboardId = myDashboard.id;
  }

  // TODO trigger fetching this when layout is non-empty (and widgetsettings is empty)
  let widgetsWithSettings: {[key: string]: WidgetSettings} = {};

  // TODO handle empty
  if (hasContent(isFetching, myDashboard, myWidgets)) {
    widgetsWithSettings = myWidgets.entities;
  }

  if (hasContent(isFetching, myDashboard, myWidgets)) {
    layout = myDashboard.layout.layout.map((widgetLayout: Layout) => ({
      ...widgetLayout,
      isDraggable: true,
      isResizable: widgetsWithSettings[widgetLayout.i as string].type === WidgetType.MAP ? true : false,
    }));
  }

  // TODO handle empty
  // TODO filter widgets that do not exists in both 'layout' and 'myWidgets'
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
          settings={settings as CollectionStatusWidgetSettings}
          dashboardId={dashboardId}
          isOpen={true}
          onCancel={closeConfigurationDialog}
          onSave={saveWidgetConfiguration}
        />
      )
    )
    .getOrElseNull();

  const editMapWidgetDialog = widgetBeingEdited
    .filter(({type}) => type === WidgetType.MAP)
    .map(
      settings => (
        <EditMapWidgetContainer
          id={settings.id}
          settings={settings as MapWidgetSettings}
          dashboardId={dashboardId}
          isOpen={true}
          onCancel={closeConfigurationDialog}
          onSave={saveWidgetConfiguration}
        />
      )
    )
    .getOrElseNull();

  const addNewWidget = newWidgetMenu((type: WidgetType) => {
    const widgetSettings: WidgetSettings =
      type === WidgetType.COLLECTION
        ? {
          dashboardId,
          id: idGenerator.uuid(),
          type,
          settings: {
            selectionInterval: {
              period: Period.latest,
            },
          },
        }
        : {
          dashboardId,
          id: idGenerator.uuid(),
          type,
          settings: {},
        };

    editWidget(Maybe.just(widgetSettings));
  });

  return (
    <PageLayout>
      <Row className="space-between">
        <MainTitle>{translate('dashboard')}</MainTitle>
        <Row>
          <Column title={firstUpperTranslated('add widget')}>
            <ActionsDropdown
              renderPopoverContent={addNewWidget}
              className="SelectionResultActionDropdown"
              icon={Add}
              iconProps={{color: colors.lightBlack, style: iconSizeMedium}}
            />
          </Column>
        </Row>
      </Row>

      <ReactGridLayout
        layout={layout}
        width={1200}
        cols={6}
        rowHeight={170}
        onLayoutChange={onLayoutChange}
        draggableHandle={'.draggableWidgetArea'}
        margin={widgetMargins}
      >
        {widgetsA}
      </ReactGridLayout>

      {editCollectionPercentageWidgetDialog}
      {editMapWidgetDialog}
    </PageLayout>
  );
};

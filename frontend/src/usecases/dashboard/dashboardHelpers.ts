import {Layout} from 'react-grid-layout';
import {Period} from '../../components/dates/dateModels';
import {idGenerator} from '../../helpers/idGenerator';
import {
  CollectionStatusWidget,
  MapWidget,
  Widget,
  WidgetMandatory,
  WidgetType
} from '../../state/domain-models/widget/widgetModels';
import {uuid} from '../../types/Types';

export const makeCollectionWidget = (id: uuid, dashboardId: uuid): CollectionStatusWidget => ({
  id,
  dashboardId,
  settings: {
    selectionInterval: {
      period: Period.yesterday,
    },
  },
  type: WidgetType.COLLECTION,
});

export const makeMapWidget = (id: uuid, dashboardId: uuid): MapWidget => ({
  id,
  dashboardId,
  settings: {},
  type: WidgetType.MAP,
});

export const makeDefaultWidget = (dashboardId: uuid, type: WidgetType): Widget => {
  if (type === WidgetType.COLLECTION) {
    return makeCollectionWidget(idGenerator.uuid(), dashboardId);
  } else if (type === WidgetType.MAP) {
    return makeMapWidget(idGenerator.uuid(), dashboardId);
  } else {
    return {...makeMapWidget(idGenerator.uuid(), dashboardId), type: WidgetType.COUNT};
  }
};

interface LayoutProps {
  w: number;
  h: number;
}

export const widgetDimensions: { [w in WidgetType]: LayoutProps } = {
  [WidgetType.MAP]: {w: 5, h: 4},
  [WidgetType.COLLECTION]: {w: 1, h: 1},
  [WidgetType.COUNT]: {w: 1, h: 1},
};

export const addToNextRow = (widgetSettings: WidgetMandatory, layout: Layout[]): Layout[] =>
  [
    ...layout,
    {
      i: widgetSettings.id.toString(),
      x: 0,
      y: layout.reduce((previous: number, {y, h}: Layout) => Math.max(previous, y + h), -1) + 1,
      w: widgetDimensions[widgetSettings.type].w,
      h: widgetDimensions[widgetSettings.type].h,
    },
  ];

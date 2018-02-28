import {PieData} from '../../../components/pie-chart-selector/PieChartSelector';
import {uuid} from '../../../types/Types';
import {ObjectsById, SelectionEntity} from '../domainModels';

export interface SelectionTreeItem {
  id: uuid;
  name: string;
  parent: {type: string; id: uuid};
  selectable: boolean;
  childNodes: {type: string; ids: uuid[]};
}

export interface SelectionTreeItemProps {
  unit: SelectionEntity;
  parentType: string;
  parent: SelectionEntity;
  selectable: boolean;
  childrenType: string;
}

export interface SelectionTreeItemsProps extends SelectionTreeItemProps {
  category: string;
  set: Set<uuid>;
}

export type SelectionTreeModel = ObjectsById<SelectionTreeItem>;

export interface SelectionTreeData {
  result: {[key: string]: uuid[]};
  entities: {[key: string]: SelectionTreeModel};
}

export interface MeterDataSummary {
  flagged: PieData;
  city: PieData;
  manufacturer: PieData;
  medium: PieData;
  status: PieData;
  alarm: PieData;
}

export type MeterDataSummaryKey = keyof MeterDataSummary;

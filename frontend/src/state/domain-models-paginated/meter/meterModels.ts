import {PieData} from '../../../components/pie-chart-selector/PieChartSelector';
import {HasId, IdNamed, uuid} from '../../../types/Types';
import {Location, ObjectsById, SelectionEntity} from '../../domain-models/domainModels';
import {Flag} from '../../domain-models/flag/flagModels';

export interface MeterStatusChangelog {
  id: uuid;
  statusId: uuid;
  name: string;
  start: string;
}

export interface Meter extends Location, HasId {
  moid: uuid;
  sapId?: uuid;
  measurementId?: uuid;
  facility: string;
  alarm: string;
  flags: Flag[];
  flagged: boolean;
  medium: string;
  manufacturer: string;
  statusChanged?: string;
  statusChangelog: MeterStatusChangelog[];
  date?: string;
  status: IdNamed;
  gatewayId: uuid;
  gatewayStatus: IdNamed;
  gatewaySerial: uuid;
  gatewayProductModel: string;
}

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

export const enum MeterStatus {
  ok = 0,
  alarm = 3,
  unknown = 4,
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

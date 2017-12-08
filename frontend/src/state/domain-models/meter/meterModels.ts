import {IdNamed, uuid} from '../../../types/Types';
import {DomainModel, Location, NormalizedState, SelectionEntity} from '../domainModels';
import {Flag} from '../flag/flagModels';
import {PieData} from '../../../components/pie-chart-selector/PieChartSelector';

export interface MeterStatusChangelog {
  id: uuid;
  meterId: uuid;
  status: IdNamed;
  date: string;
}

export interface Meter extends Location {
  id: uuid;
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
  gatewayProductModel: string;
}

export type MetersState = NormalizedState<Meter>;

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

export type SelectionTreeModel = DomainModel<SelectionTreeItem>;

export interface SelectionTreeData {
  result: DomainModel<uuid[]>;
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

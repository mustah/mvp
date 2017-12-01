import {IdNamed, uuid} from '../../../types/Types';
import {DomainModel, Location, NormalizedState, SelectionEntity} from '../domainModels';
import {Flag} from '../flag/flagModels';

export interface Meter extends Location {
  id: uuid;
  moid: string;
  facility: string;
  alarm: string;
  flags: Flag[];
  flagged: boolean;
  medium: string;
  manufacturer: string;
  statusChanged?: string;
  date?: string;
  status: IdNamed;
  gatewayId: string;
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
  entities: SelectionTreeModel;
}

export const enum meterStatus {
  ok = 0,
  alarm = 3,
  unknown = 4,
}

export const meterStatusLabels =  {
  [meterStatus.ok]: 'ok',
  [meterStatus.alarm]: 'alarms',
  [meterStatus.unknown]: 'unknown',
};

import {uuid} from '../../../types/Types';
import {MapMarker} from '../../../usecases/map/mapModels';
import {NormalizedState, SelectionEntity} from '../domainModels';
import {Flag} from '../flag/flagModels';

export interface Meter extends MapMarker {
  id: uuid;
  moid: string;
  facility: string;
  flags: Flag[];
  medium: string;
  manufacturer: string;
  gatewayId: string;
  statusChanged: string;
  date?: string;
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

export interface SelectionTreeModel {
  [key: string]: Array<{
    id: uuid;
    name: string;
    selectable: boolean;
    parent: {type: string; id: uuid};
    childNodes: {type: string; ids: uuid[]};
  }>;
}

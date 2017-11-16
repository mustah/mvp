import {uuid} from '../../../types/Types';
import {MapMarker} from '../../../usecases/map/mapModels';
import {NormalizedState, SelectionEntity} from '../domainModels';
import {Flag} from '../flag/flagModels';

export interface Meter extends MapMarker {
  id: string;
  facility: string;
  flags: Flag[];
  medium: string;
  manufacturer: string;
  gatewayId: string;
  statusChanged: string;
}

export type MetersState = NormalizedState<Meter>;

export interface SidebarItem {
  id: uuid;
  name: string;
  parent: {type: string; id: uuid};
  selectable: boolean;
  childNodes: {type: string; ids: uuid[]};
}

export interface SidebarItemProps {
  unit: SelectionEntity;
  parentType: string;
  parent: SelectionEntity;
  selectable: boolean;
  childrenType: string;
}

export interface SidebarItemsProps extends SidebarItemProps {
  category: string;
  set: Set<uuid>;
}

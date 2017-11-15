import {IdNamed, uuid} from '../../../types/Types';
import {MapMarker} from '../../../usecases/map/mapModels';
import {Address} from '../domainModelsModels';
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

export interface Meters {
  result: uuid[];
  entities: {
    meters: {[key: string]: Meter};
  };
}

export interface MetersState extends Meters {
  isFetching: boolean;
  total: number;
}

type Unit = IdNamed | Address;

export interface SidebarItem {
  id: uuid;
  name: string;
  parent: {type: string; id: uuid};
  selectable: boolean;
  childNodes: {type: string; ids: uuid[]};
}

export interface SidebarItemProps {
  unit: Unit;
  parentType: string;
  parent: Unit;
  selectable: boolean;
  childrenType: string;
}

export interface SidebarItemsProps extends SidebarItemProps {
  category: string;
  set: Set<uuid>;
}

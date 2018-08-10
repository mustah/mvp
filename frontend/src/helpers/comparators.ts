import {SelectionListItem} from '../state/user-selection/userSelectionModels';
import {IdNamed} from '../types/Types';

const isNameUnknown = (o1: IdNamed): boolean => o1.name === 'unknown';

export const byNameAsc = (o1: IdNamed, o2: IdNamed): number =>
  (o1.name > o2.name) ? 1 : ((o2.name > o1.name) ? -1 : 0);

export const unknownNameFirstThenByNameAsc = (o1: IdNamed, o2: IdNamed): number =>
  isNameUnknown(o1) ? -1 : isNameUnknown(o2) ? 1 : byNameAsc(o1, o2);

export const selectedFirstThenUnknownByNameAsc = (o1: SelectionListItem, o2: SelectionListItem): number =>
  o1.selected ? -1 : !o1.selected && o2.selected ? 1 : unknownNameFirstThenByNameAsc(o1, o2);

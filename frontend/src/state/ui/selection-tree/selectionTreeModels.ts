import {Identifiable, uuid} from '../../../types/Types';

export interface SelectionTreeUiState {
  openListItems: uuid[];
}

export interface SelectionTreeViewComposite extends Identifiable {
  text: string;
  expanded?: boolean;
  type: SelectionTreeItemType;
  items: SelectionTreeViewComposite[];
}

export enum SelectionTreeItemType {
  meter = 'meter',
  city = 'city',
  address = 'address'
}

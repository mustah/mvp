import {IdNamed, uuid} from '../../../types/Types';

export interface SelectionParameter extends IdNamed {
  attribute: string;
}

export interface SelectionEntity {
  [key: string]: IdNamed;
}

export interface SelectedIds {
  [key: string]: uuid[];
}

export interface SelectionOptions {
  entities: SelectionEntity;
  result: SelectedIds;
}

export interface SelectionState extends SelectionOptions {
  isFetching: boolean;
  selected: SelectedIds;
}

export interface SearchParameterState {
  selection: SelectionState;
}

import {IdNamed, uuid} from '../../../types/Types';

export interface SelectionParameter extends IdNamed {
  entity: string;
}

export interface SelectionOptionEntity {
  [key: string]: IdNamed;
}

export interface SelectionResult {
  [key: string]: uuid[];
}

export interface SelectionOptions {
  entities: SelectionOptionEntity;
  result: SelectionResult;
}

export interface SearchParameterState {
  selected?: any; // TODO[!must!] fix this
}

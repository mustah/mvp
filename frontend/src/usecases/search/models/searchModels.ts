import {IdNamed, uuid} from '../../../types/Types';

export interface SearchParameter extends IdNamed {
  entity: string;
}

export interface SearchOptionEntity {
  [key: string]: IdNamed;
}

export interface SearchResult {
  [key: string]: uuid[];
}

export interface SearchOptions {
  entities: SearchOptionEntity;
  result: SearchResult;
}

export interface SearchSelection {
  selected: uuid[];
  unselected: uuid[];
}

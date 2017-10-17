import {IdNamed, uuid} from '../../../types/Types';

export interface SearchParameter extends IdNamed {
  entity: string;
}

export interface SearchOption {
  [key: string]: IdNamed;
}

export interface SearchOptionResult {
  [key: string]: uuid[];
}

export interface SearchOptions {
  entities: SearchOption;
  result: SearchOptionResult;
}

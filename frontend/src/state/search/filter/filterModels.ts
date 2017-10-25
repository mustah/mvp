import {uuid} from '../../../types/Types';

export interface FilterState {
  [category: string]: Set<uuid>;
}

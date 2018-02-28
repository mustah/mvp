import {HasId, uuid} from '../../../types/Types';

export interface Organisation extends HasId {
  code: uuid;
  name: string;
}

import {Identifiable, uuid} from '../../../types/Types';

export interface Organisation extends Identifiable {
  code: uuid;
  name: string;
}

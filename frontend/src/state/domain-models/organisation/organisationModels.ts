import {Identifiable, uuid} from '../../../types/Types';

export interface Organisation extends Identifiable {
  slug: uuid;
  name: string;
}

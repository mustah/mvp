import {IdNamed, uuid} from '../../../types/Types';

export interface Organisation extends IdNamed {
  slug: uuid;
  parent?: Organisation;
}

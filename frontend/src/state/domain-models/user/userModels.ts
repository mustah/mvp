import {HasId, uuid} from '../../../types/Types';
import {NormalizedState} from '../domainModels';

export interface Organisation extends HasId {
  code: uuid;
  name: string;
}

export interface User {
  id: uuid;
  name: string;
  email: string;
  organisation: Organisation;
  password?: string;
  roles: Role[];
}

export type UserState = NormalizedState<User>;

export const enum Role {
  ADMIN = 'ADMIN',
  USER = 'USER',
  SUPER_ADMIN = 'SUPER_ADMIN',
}

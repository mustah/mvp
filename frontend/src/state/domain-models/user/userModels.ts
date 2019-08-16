import {uuid} from '../../../types/Types';
import {LanguageCode} from '../../language/languageModels';
import {NormalizedState} from '../domainModels';
import {Organisation} from '../organisation/organisationModels';

export interface User {
  id: uuid;
  name: string;
  email: string;
  language: LanguageCode;
  organisation: Organisation;
  password?: string; // TODO: should this be here? Will that be of use in the future, ex: changing password?
  roles: Role[];
}

export type UserState = NormalizedState<User>;

export const enum Role {
  MVP_ADMIN = 'MVP_ADMIN',
  MVP_USER = 'MVP_USER',
  SUPER_ADMIN = 'SUPER_ADMIN',
  OTC_ADMIN = 'OTC_ADMIN',
  OTC_USER = 'OTC_USER',
}

export interface Password {
  password: string;
}

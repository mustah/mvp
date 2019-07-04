import {makeDataFormatter} from '../domainModelSchema';
import {User} from './userModels';

export const usersDataFormatter = makeDataFormatter<User>('users');

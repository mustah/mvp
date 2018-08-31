import {normalize, schema} from 'normalizr';
import {Normalized} from '../domainModels';
import {DataFormatter} from '../domainModelsActions';
import {User} from './userModels';

const userSchema = [new schema.Entity('users')];

export const usersDataFormatter: DataFormatter<Normalized<User>> =
  (response) => normalize(response, userSchema);

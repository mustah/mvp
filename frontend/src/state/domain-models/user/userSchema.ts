import {normalize, schema} from 'normalizr';
import {DataFormatter} from '../domainModelsActions';

const userSchema = [new schema.Entity('users')];

export const usersDataFormatter: DataFormatter =
  (response) => normalize(response, userSchema);

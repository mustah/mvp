import {normalize, schema} from 'normalizr';
import {DataFormatter} from '../domainModelsActions';

const organisationSchema = [new schema.Entity('organisations')];

export const organisationsDataFormatter: DataFormatter =
  (response) => normalize(response, organisationSchema);

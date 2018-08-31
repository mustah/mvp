import {normalize, schema} from 'normalizr';
import {Normalized} from '../domainModels';
import {DataFormatter} from '../domainModelsActions';
import {Organisation} from './organisationModels';

const organisationSchema = [new schema.Entity('organisations')];

export const organisationsDataFormatter: DataFormatter<Normalized<Organisation>> =
  (response) => normalize(response, organisationSchema);

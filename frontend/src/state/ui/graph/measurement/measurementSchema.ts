import {normalize, Schema, schema} from 'normalizr';
import {NormalizedPaginated} from '../../../domain-models-paginated/paginatedDomainModels';
import {DataFormatter} from '../../../domain-models/domainModelsActions';
import {Measurement} from './measurementModels';

const measurementSchemaEntity = new schema.Entity('measurements', {});
const measurementSchema: Schema = {content: [measurementSchemaEntity]};
export const measurementDataFormatter: DataFormatter<NormalizedPaginated<Measurement>> =
  (response) => normalize(response, measurementSchema) as NormalizedPaginated<Measurement>;

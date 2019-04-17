import {normalize, Schema, schema} from 'normalizr';
import {CollectionStat} from '../../domain-models/collection-stat/collectionStatModels';
import {DataFormatter} from '../../domain-models/domainModelsActions';
import {NormalizedPaginated} from '../paginatedDomainModels';

const collectionStatSchemaEntity = new schema.Entity('collectionStatFacilities', {});
const collectionStatSchema: Schema = {content: [collectionStatSchemaEntity]};
export const collectionStatDataFormatter: DataFormatter<NormalizedPaginated<CollectionStat>> =
  (response) => normalize(response, collectionStatSchema) as NormalizedPaginated<CollectionStat>;

import {normalize, Schema, schema} from 'normalizr';
import {NormalizedPaginated} from '../../domain-models-paginated/paginatedDomainModels';
import {CollectionStat} from '../../domain-models/collection-stat/collectionStatModels';
import {DataFormatter} from '../../domain-models/domainModelsActions';

const collectionStatSchemaEntity = new schema.Entity('collectionStatFacilities', {});
const collectionStatSchema: Schema = {content: [collectionStatSchemaEntity]};
export const collectionStatDataFormatter: DataFormatter<NormalizedPaginated<CollectionStat>> =
  (response) => normalize(response, collectionStatSchema) as NormalizedPaginated<CollectionStat>;

import {normalize, schema} from 'normalizr';
import {Normalized} from '../domainModels';
import {DataFormatter} from '../domainModelsActions';
import {CollectionStat} from './collectionStatModels';

const collectionStatDateSchema = [new schema.Entity('collectionStats')];

export const collectionStatDateDataFormatter: DataFormatter<Normalized<CollectionStat>> =
  (response) => normalize(response, collectionStatDateSchema);

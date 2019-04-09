import {EndPoints} from '../../../services/endPoints';
import {ModelSectors} from '../../../types/Types';
import {
  clearError, fetchIfNeededForSector
} from '../../domain-models/domainModelsActions';
import {CollectionStat} from './collectionStatModels';
import {collectionStatDateDataFormatter} from './collectionStatSchema';

export const meterCollectionStatClearError = clearError(ModelSectors.meterCollection);
export const collectionStatClearError = clearError(EndPoints.collectionStatFacility);

export const fetchCollectionStats = fetchIfNeededForSector<CollectionStat>(
  ModelSectors.collection,
  EndPoints.collectionStatDate,
  'collectionStats',
  collectionStatDateDataFormatter,
);

export const fetchMeterCollectionStats = fetchIfNeededForSector<CollectionStat>(
  ModelSectors.meterCollection,
  EndPoints.collectionStatDate,
  'meterCollectionStats',
  collectionStatDateDataFormatter,
);

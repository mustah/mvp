import {EndPoints} from '../../../services/endPoints';
import {Sectors} from '../../../types/Types';
import {clearError, fetchIfNeeded, fetchIfNeededForSector} from '../domainModelsActions';
import {makeDataFormatter} from '../domainModelSchema';
import {CollectionStat} from './collectionStatModels';

export const meterCollectionStatClearError = clearError(Sectors.meterCollection);

export const fetchCollectionStats = fetchIfNeededForSector<CollectionStat>(
  Sectors.collection,
  EndPoints.collectionStatDate,
  'collectionStats',
  makeDataFormatter('collectionStats'),
);

export const fetchMeterCollectionStats = fetchIfNeededForSector<CollectionStat>(
  Sectors.meterCollection,
  EndPoints.collectionStatDate,
  'meterCollectionStats',
  makeDataFormatter('collectionStats'),
);

export const fetchAllCollectionStats = fetchIfNeeded<CollectionStat>(
  EndPoints.collectionStats,
  'allCollectionStats',
  makeDataFormatter('allCollectionStats'),
);

import {EndPoints} from '../../../services/endPoints';
import {Sectors} from '../../../types/Types';
import {clearError, fetchIfNeededForSector} from '../domainModelsActions';
import {CollectionStat} from './collectionStatModels';
import {collectionStatDateDataFormatter} from './collectionStatSchema';

export const meterCollectionStatClearError = clearError(Sectors.meterCollection);
export const collectionStatClearError = clearError(EndPoints.collectionStatFacility);

export const fetchCollectionStats = fetchIfNeededForSector<CollectionStat>(
  Sectors.collection,
  EndPoints.collectionStatDate,
  'collectionStats',
  collectionStatDateDataFormatter,
);

export const fetchMeterCollectionStats = fetchIfNeededForSector<CollectionStat>(
  Sectors.meterCollection,
  EndPoints.collectionStatDate,
  'meterCollectionStats',
  collectionStatDateDataFormatter,
);

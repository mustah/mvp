import {EndPoints} from '../../../services/endPoints';
import {clearError, fetchIfNeeded} from '../../domain-models/domainModelsActions';
import {CollectionStat} from './collectionStatModels';
import {collectionStatDateDataFormatter} from './collectionStatSchema';

export const collectionStatClearError = clearError(EndPoints.collectionStatFacility);

export const fetchCollectionStats = fetchIfNeeded<CollectionStat>(
  EndPoints.collectionStatDate,
  'collectionStats',
  collectionStatDateDataFormatter,
);

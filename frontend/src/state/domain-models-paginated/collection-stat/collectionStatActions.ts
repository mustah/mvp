import {EndPoints} from '../../../services/endPoints';
import {CollectionStat} from '../../domain-models/collection-stat/collectionStatModels';
import {updatePageMetaData} from '../../ui/pagination/paginationActions';
import {NormalizedPaginated} from '../paginatedDomainModels';
import {fetchIfNeeded, sortTable} from '../paginatedDomainModelsActions';
import {collectionStatDataFormatter} from './collectionStatSchema';

export const fetchCollectionStatsFacilityPaged = fetchIfNeeded<CollectionStat>(
  EndPoints.collectionStatFacility,
  collectionStatDataFormatter,
  'collectionStatFacilities',
  {
    afterSuccess: ({result}: NormalizedPaginated<CollectionStat>, dispatch) =>
      dispatch(updatePageMetaData({entityType: 'collectionStatFacilities', ...result})),
  },
);

export const sortTableCollectionStats = sortTable(EndPoints.collectionStatFacility);

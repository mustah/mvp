import {EndPoints} from '../../../services/endPoints';
import {PagedDomainModelsSectors} from '../../../types/Types';
import {CollectionStat} from '../../domain-models/collection-stat/collectionStatModels';
import {updatePageMetaData} from '../../ui/pagination/paginationActions';
import {NormalizedPaginated} from '../paginatedDomainModels';
import {fetchIfNeededForSector, sortTableAction} from '../paginatedDomainModelsActions';
import {collectionStatDataFormatter} from './collectionStatSchema';

export const fetchCollectionStatsFacilityPaged = fetchIfNeededForSector<CollectionStat>(
  PagedDomainModelsSectors.collectionStatFacilities,
  EndPoints.collectionStatFacility,
  collectionStatDataFormatter,
  'collectionStatFacilities',
  {
    afterSuccess: ({result}: NormalizedPaginated<CollectionStat>, dispatch) =>
      dispatch(updatePageMetaData({entityType: 'collectionStatFacilities', ...result})),
  },
);

export const fetchMeterCollectionStatsFacilityPaged = fetchIfNeededForSector<CollectionStat>(
  PagedDomainModelsSectors.meterCollectionStatFacilities,
  EndPoints.collectionStatFacility,
  collectionStatDataFormatter,
  'meterCollectionStatFacilities',
  {
    afterSuccess: ({result}: NormalizedPaginated<CollectionStat>, dispatch) =>
      dispatch(updatePageMetaData({entityType: 'meterCollectionStatFacilities', ...result})),
  },
);

export const sortTableCollectionStats = sortTableAction(EndPoints.collectionStatFacility);

export const sortTableMeterCollectionStats = sortTableAction(PagedDomainModelsSectors.meterCollectionStatFacilities);

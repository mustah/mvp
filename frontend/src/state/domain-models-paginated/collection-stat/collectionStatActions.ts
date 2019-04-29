import {EndPoints} from '../../../services/endPoints';
import {Sectors} from '../../../types/Types';
import {CollectionStat} from '../../domain-models/collection-stat/collectionStatModels';
import {updatePageMetaData} from '../../ui/pagination/paginationActions';
import {NormalizedPaginated} from '../paginatedDomainModels';
import {fetchIfNeeded, sortTableAction} from '../paginatedDomainModelsActions';
import {collectionStatDataFormatter} from './collectionStatSchema';

export const fetchCollectionStatsFacilityPaged = fetchIfNeeded<CollectionStat>(
  Sectors.collectionStatFacilities,
  EndPoints.collectionStatFacility,
  collectionStatDataFormatter,
  'collectionStatFacilities',
  {
    afterSuccess: ({result}: NormalizedPaginated<CollectionStat>, dispatch) =>
      dispatch(updatePageMetaData({entityType: 'collectionStatFacilities', ...result})),
  },
);

export const fetchMeterCollectionStatsFacilityPaged = fetchIfNeeded<CollectionStat>(
  Sectors.meterCollectionStatFacilities,
  EndPoints.collectionStatFacility,
  collectionStatDataFormatter,
  'meterCollectionStatFacilities',
  {
    afterSuccess: ({result}: NormalizedPaginated<CollectionStat>, dispatch) =>
      dispatch(updatePageMetaData({entityType: 'meterCollectionStatFacilities', ...result})),
  },
);

export const sortTableCollectionStats = sortTableAction(Sectors.collectionStatFacilities);

export const sortTableMeterCollectionStats = sortTableAction(Sectors.meterCollectionStatFacilities);

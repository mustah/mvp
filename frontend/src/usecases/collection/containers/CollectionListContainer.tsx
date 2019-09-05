import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {fetchCollectionStatsFacilityPaged} from '../../../state/domain-models-paginated/collection-stat/collectionStatActions';
import {sortCollectionStats as sortTable} from '../../../state/domain-models-paginated/paginatedDomainModelsActions';
import {
  getCollectionStats,
  getPageIsFetching
} from '../../../state/domain-models-paginated/paginatedDomainModelsSelectors';
import {fetchAllCollectionStats} from '../../../state/domain-models/collection-stat/collectionStatActions';
import {getAllEntities} from '../../../state/domain-models/domainModelsSelectors';
import {changePage} from '../../../state/ui/pagination/paginationActions';
import {Pagination} from '../../../state/ui/pagination/paginationModels';
import {
  getCollectionStatsExcelExportParameters,
  getCollectionStatsParameters
} from '../../../state/user-selection/userSelectionSelectors';
import {collectionStatsExportToExcelSuccess as exportToExcelSuccess} from '../collectionActions';
import {
  CollectionListContent,
  CollectionStatsDispatchToProps,
  CollectionStatsStateToProps
} from '../components/CollectionListContent';

const mapStateToProps = (rootState: RootState): CollectionStatsStateToProps => {
  const {
    collection: {isExportingToExcel},
    domainModels: {allCollectionStats},
    meterDetail: {selectedMeterId: selectedItemId},
    paginatedDomainModels: {collectionStatFacilities},
    summary: {payload: {numMeters}},
    ui: {pagination: paginationState},
  }: RootState = rootState;

  const pagination: Pagination = paginationState.collectionStatFacilities;
  const {page, totalElements} = pagination;
  const {sort} = collectionStatFacilities;
  const isFetching = getPageIsFetching(collectionStatFacilities, page);

  return ({
    entityType: 'collectionStatFacilities',
    excelExportParameters: getCollectionStatsExcelExportParameters(rootState),
    hasContent: isFetching || totalElements > 0 || numMeters > 0,
    isExportingToExcel,
    isFetching,
    items: getCollectionStats(collectionStatFacilities),
    itemsToExport: getAllEntities(allCollectionStats),
    parameters: getCollectionStatsParameters(rootState),
    pagination,
    selectedItemId,
    sort,
  });
};

const mapDispatchToProps = (dispatch): CollectionStatsDispatchToProps => bindActionCreators({
  changePage,
  exportToExcelSuccess,
  fetchAllCollectionStats,
  fetchPaginated: fetchCollectionStatsFacilityPaged,
  sortTable,
}, dispatch);

export const CollectionListContainer =
  connect<CollectionStatsStateToProps, CollectionStatsDispatchToProps>(
    mapStateToProps,
    mapDispatchToProps
  )(CollectionListContent);

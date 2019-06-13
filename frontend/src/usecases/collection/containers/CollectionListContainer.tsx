import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {makeCollectionPeriodParametersOf} from '../../../helpers/urlFactory';
import {RootState} from '../../../reducers/rootReducer';
import {fetchCollectionStatsFacilityPaged} from '../../../state/domain-models-paginated/collection-stat/collectionStatActions';
import {sortCollectionStats} from '../../../state/domain-models-paginated/paginatedDomainModelsActions';
import {
  getCollectionStats,
  getPageIsFetching
} from '../../../state/domain-models-paginated/paginatedDomainModelsSelectors';
import {changePage} from '../../../state/ui/pagination/paginationActions';
import {Pagination} from '../../../state/ui/pagination/paginationModels';
import {getPaginatedCollectionStatParameters} from '../../../state/user-selection/userSelectionSelectors';
import {Sectors} from '../../../types/Types';
import {exportToExcelSuccess} from '../collectionActions';
import {CollectionListContent, DispatchToProps, StateToProps} from '../components/CollectionListContent';

const mapStateToProps = ({
  collection: {isExportingToExcel, timePeriod},
  meterDetail: {selectedMeterId: selectedItemId},
  paginatedDomainModels: {collectionStatFacilities},
  search: {validation: {query}},
  summary: {payload: {numMeters}},
  ui: {pagination: paginationState},
  userSelection: {userSelection}
}: RootState): StateToProps => {
  const pagination: Pagination = paginationState.collectionStatFacilities;
  const {page, totalElements} = pagination;
  const {sort} = collectionStatFacilities;
  const isFetching = getPageIsFetching(collectionStatFacilities, page);

  return ({
    entityType: 'collectionStatFacilities',
    hasContent: isFetching || totalElements > 0 || numMeters > 0,
    isExportingToExcel,
    isFetching,
    items: getCollectionStats(collectionStatFacilities),
    parameters: `${makeCollectionPeriodParametersOf(timePeriod)}&${getPaginatedCollectionStatParameters({
      sort,
      pagination,
      userSelection,
      query,
    })}`,
    pagination,
    selectedItemId,
    sort,
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changePage,
  exportToExcelSuccess: exportToExcelSuccess(Sectors.collection),
  fetchCollectionStatsFacilityPaged,
  sortTable: sortCollectionStats,
}, dispatch);

export const CollectionListContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(CollectionListContent);

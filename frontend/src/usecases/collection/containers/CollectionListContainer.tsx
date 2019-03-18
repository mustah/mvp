import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {
  fetchCollectionStatsFacilityPaged,
  sortTableCollectionStats
} from '../../../state/domain-models-paginated/collection-stat/collectionStatActions';
import {
  getPageError,
  getPageIsFetching,
  getPageResult,
  getPaginatedEntities
} from '../../../state/domain-models-paginated/paginatedDomainModelsSelectors';
import {collectionStatClearError} from '../../../state/domain-models/collection-stat/collectionStatActions';
import {CollectionStat} from '../../../state/domain-models/collection-stat/collectionStatModels';
import {changePage} from '../../../state/ui/pagination/paginationActions';
import {EntityTypes, Pagination} from '../../../state/ui/pagination/paginationModels';
import {getPagination} from '../../../state/ui/pagination/paginationSelectors';
import {getPaginatedCollectionStatParameters} from '../../../state/user-selection/userSelectionSelectors';
import {ComponentId} from '../../../types/Types';
import {
  CollectionDispatchToProps,
  CollectionListContent,
  CollectionStateToProps
} from '../components/CollectionListContent';

const mapStateToProps = (
  {
    auth: {user},
    userSelection: {userSelection},
    paginatedDomainModels: {collectionStatFacilities},
    ui: {pagination: paginationModel},
    search: {validation: {query}},
    collection: {timePeriod}
  }: RootState,
  {componentId}: ComponentId,
): CollectionStateToProps => {
  const entityType: EntityTypes = 'collectionStatFacilities';
  const pagination: Pagination = getPagination({componentId, entityType, pagination: paginationModel});
  const {page} = pagination;
  const {sort} = collectionStatFacilities;

  return ({
    entities: getPaginatedEntities<CollectionStat>(collectionStatFacilities),
    result: getPageResult(collectionStatFacilities, page),
    parameters: getPaginatedCollectionStatParameters({
      sort,
      pagination,
      userSelection,
      query,
      period: {customDateRange: Maybe.maybe(timePeriod.customDateRange), period: timePeriod.period}
    }),
    sort,
    isFetching: getPageIsFetching(collectionStatFacilities, page),
    pagination,
    error: getPageError<CollectionStat>(collectionStatFacilities, page),
    entityType,
    timePeriod,
  });
};

const mapDispatchToProps = (dispatch): CollectionDispatchToProps => bindActionCreators({
  changePage,
  sortTable: sortTableCollectionStats,
  clearError: collectionStatClearError,
  fetchCollectionStatsFacilityPaged,
}, dispatch);

export const CollectionListContainer = connect<CollectionStateToProps, CollectionDispatchToProps>(
  mapStateToProps,
  mapDispatchToProps
)(
  CollectionListContent
);

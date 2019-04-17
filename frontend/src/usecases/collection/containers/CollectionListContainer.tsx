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
import {ObjectsById} from '../../../state/domain-models/domainModels';
import {changePage} from '../../../state/ui/pagination/paginationActions';
import {
  ApiRequestSortingOptions,
  EntityTypes,
  OnChangePage,
  Pagination
} from '../../../state/ui/pagination/paginationModels';
import {getPagination} from '../../../state/ui/pagination/paginationSelectors';
import {SelectionInterval} from '../../../state/user-selection/userSelectionModels';
import {getPaginatedCollectionStatParameters} from '../../../state/user-selection/userSelectionSelectors';
import {
  Callback,
  CallbackWith,
  ClearErrorPaginated,
  ComponentId,
  EncodedUriParameters,
  ErrorResponse,
  FetchPaginated,
  Sectors,
  uuid
} from '../../../types/Types';
import {exportToExcelSuccess} from '../collectionActions';
import {CollectionListContent} from '../components/CollectionListContent';

export interface StateToProps {
  result: uuid[];
  entities: ObjectsById<CollectionStat>;
  isExportingToExcel: boolean;
  isFetching: boolean;
  parameters: EncodedUriParameters;
  sort?: ApiRequestSortingOptions[];
  pagination: Pagination;
  error: Maybe<ErrorResponse>;
  entityType: EntityTypes;
  timePeriod: SelectionInterval;
}

export interface DispatchToProps {
  changePage: OnChangePage;
  clearError: ClearErrorPaginated;
  exportToExcelSuccess: Callback;
  fetchCollectionStatsFacilityPaged: FetchPaginated;
  sortTable: CallbackWith<ApiRequestSortingOptions[]>;
}

const mapStateToProps = (
  {
    userSelection: {userSelection},
    paginatedDomainModels: {collectionStatFacilities},
    ui: {pagination: paginationModel},
    search: {validation: {query}},
    collection: {isExportingToExcel, timePeriod}
  }: RootState,
  {componentId}: ComponentId,
): StateToProps => {
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
    isExportingToExcel,
    isFetching: getPageIsFetching(collectionStatFacilities, page),
    pagination,
    error: getPageError<CollectionStat>(collectionStatFacilities, page),
    entityType,
    timePeriod,
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changePage,
  clearError: collectionStatClearError,
  exportToExcelSuccess: exportToExcelSuccess(Sectors.collection),
  fetchCollectionStatsFacilityPaged,
  sortTable: sortTableCollectionStats,
}, dispatch);

export const CollectionListContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(CollectionListContent);

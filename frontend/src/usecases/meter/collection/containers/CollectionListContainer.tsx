import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {Maybe} from '../../../../helpers/Maybe';
import {encodedUriParametersFrom, RequestParameter} from '../../../../helpers/urlFactory';
import {RootState} from '../../../../reducers/rootReducer';
import {
  fetchMeterCollectionStatsFacilityPaged,
  sortTableMeterCollectionStats
} from '../../../../state/domain-models-paginated/collection-stat/collectionStatActions';
import {
  getPageError,
  getPageIsFetching,
  getPageResult,
  getPaginatedEntities
} from '../../../../state/domain-models-paginated/paginatedDomainModelsSelectors';
import {collectionStatClearError} from '../../../../state/domain-models/collection-stat/collectionStatActions';
import {CollectionStat} from '../../../../state/domain-models/collection-stat/collectionStatModels';
import {changePage} from '../../../../state/ui/pagination/paginationActions';
import {EntityTypes, Pagination} from '../../../../state/ui/pagination/paginationModels';
import {getPagination} from '../../../../state/ui/pagination/paginationSelectors';
import {getPaginatedCollectionStatParameters} from '../../../../state/user-selection/userSelectionSelectors';
import {EncodedUriParameters, Sectors, uuid} from '../../../../types/Types';
import {exportToExcelSuccess} from '../../../collection/collectionActions';
import {CollectionListContent} from '../../../collection/components/CollectionListContent';
import {DispatchToProps, StateToProps} from '../../../collection/containers/CollectionListContainer';

interface OwnProps {
  meterId: uuid;
}

const mapStateToProps = (
  {
    userSelection: {userSelection},
    paginatedDomainModels: {meterCollectionStatFacilities},
    ui: {pagination: paginationModel},
    search: {validation: {query}},
    meterCollection: {isExportingToExcel, timePeriod}
  }: RootState,
  {meterId}: OwnProps,
): StateToProps => {
  const entityType: EntityTypes = 'meterCollectionStatFacilities';
  const pagination: Pagination = getPagination({entityType, pagination: paginationModel});
  const {page} = pagination;
  const {sort} = meterCollectionStatFacilities;

  const parameters: EncodedUriParameters = encodedUriParametersFrom([
    `${RequestParameter.logicalMeterId}=${meterId}`,
    getPaginatedCollectionStatParameters({
      sort,
      pagination,
      userSelection,
      query,
      period: {customDateRange: Maybe.maybe(timePeriod.customDateRange), period: timePeriod.period}
    })
  ]);

  return ({
    entities: getPaginatedEntities<CollectionStat>(meterCollectionStatFacilities),
    result: getPageResult(meterCollectionStatFacilities, page),
    parameters,
    sort,
    isExportingToExcel,
    isFetching: getPageIsFetching(meterCollectionStatFacilities, page),
    pagination,
    error: getPageError<CollectionStat>(meterCollectionStatFacilities, page),
    entityType,
    timePeriod,
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changePage,
  clearError: collectionStatClearError,
  exportToExcelSuccess: exportToExcelSuccess(Sectors.meterCollection),
  fetchCollectionStatsFacilityPaged: fetchMeterCollectionStatsFacilityPaged,
  sortTable: sortTableMeterCollectionStats,
}, dispatch);

export const CollectionListContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(CollectionListContent);

import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {
  encodedUriParametersFrom,
  makeCollectionPeriodParametersOf,
  RequestParameter
} from '../../../../helpers/urlFactory';
import {RootState} from '../../../../reducers/rootReducer';
import {
  fetchMeterCollectionStatsFacilityPaged,
  sortTableMeterCollectionStats
} from '../../../../state/domain-models-paginated/collection-stat/collectionStatActions';
import {
  getPageError,
  getPageIsFetching,
  getPaginatedResult
} from '../../../../state/domain-models-paginated/paginatedDomainModelsSelectors';
import {collectionStatClearError} from '../../../../state/domain-models/collection-stat/collectionStatActions';
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
    makeCollectionPeriodParametersOf(timePeriod),
    getPaginatedCollectionStatParameters({
      sort,
      pagination,
      userSelection,
      query,
    })
  ]);

  return ({
    entities: meterCollectionStatFacilities.entities,
    result: getPaginatedResult(meterCollectionStatFacilities, page),
    parameters,
    sort,
    isExportingToExcel,
    isFetching: getPageIsFetching(meterCollectionStatFacilities, page),
    pagination,
    error: getPageError({page, state: meterCollectionStatFacilities}),
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

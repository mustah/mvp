import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {noop} from '../../../../helpers/commonHelpers';
import {
  encodedUriParametersFrom,
  makeCollectionPeriodParametersOf,
  RequestParameter
} from '../../../../helpers/urlFactory';
import {RootState} from '../../../../reducers/rootReducer';
import {fetchMeterCollectionStatsFacilityPaged as fetchCollectionStatsFacilityPaged} from '../../../../state/domain-models-paginated/collection-stat/collectionStatActions';
import {sortMeterCollectionStats as sortTable} from '../../../../state/domain-models-paginated/paginatedDomainModelsActions';
import {
  getCollectionStats,
  getPageIsFetching
} from '../../../../state/domain-models-paginated/paginatedDomainModelsSelectors';
import {changePage} from '../../../../state/ui/pagination/paginationActions';
import {Pagination} from '../../../../state/ui/pagination/paginationModels';
import {getPaginatedApiParameters} from '../../../../state/user-selection/userSelectionSelectors';
import {EncodedUriParameters, uuid} from '../../../../types/Types';
import {meterCollectionStatsExportToExcelSuccess as exportToExcelSuccess} from '../../../collection/collectionActions';
import {
  CollectionListContent,
  DispatchToProps,
  StateToProps
} from '../../../collection/components/CollectionListContent';

interface OwnProps {
  meterId: uuid;
}

const mapStateToProps = (
  {
    meterDetail: {selectedMeterId: selectedItemId},
    meterCollection: {isExportingToExcel, timePeriod},
    paginatedDomainModels: {meterCollectionStatFacilities},
    search: {validation: {query}},
    summary: {payload: {numMeters}},
    ui: {pagination: paginationState},
    userSelection: {userSelection}
  }: RootState,
  {meterId}: OwnProps,
): StateToProps => {
  const pagination: Pagination = paginationState.meterCollectionStatFacilities;
  const {page, totalElements} = pagination;
  const {sort} = meterCollectionStatFacilities;

  const parameters: EncodedUriParameters = encodedUriParametersFrom([
    `${RequestParameter.logicalMeterId}=${meterId}`,
    makeCollectionPeriodParametersOf(timePeriod),
    getPaginatedApiParameters({
      sort,
      pagination,
      userSelection,
      query,
    })
  ]);
  const isFetching = getPageIsFetching(meterCollectionStatFacilities, page);

  return ({
    entityType: 'meterCollectionStatFacilities',
    excelExportParameters: '',
    hasContent: isFetching || totalElements > 0 || numMeters > 0,
    isExportingToExcel,
    isFetching,
    items: getCollectionStats(meterCollectionStatFacilities),
    itemsToExport: [],
    parameters,
    pagination,
    selectedItemId,
    sort,
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changePage,
  exportToExcelSuccess,
  fetchAllCollectionStats: noop,
  fetchCollectionStatsFacilityPaged,
  sortTable,
}, dispatch);

export const CollectionListContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(CollectionListContent);

import {connect} from 'react-redux';
import {bindActionCreators, Dispatch} from 'redux';
import {MeterListContent, MeterListDispatchToProps, MeterListStateToProps} from '../components/meters/MeterListContent';
import {RootState} from '../reducers/rootReducer';
import {
  clearErrorMeters,
  deleteMeter,
  fetchMeters,
  sortTableMeters
} from '../state/domain-models-paginated/meter/meterApiActions';
import {Meter} from '../state/domain-models-paginated/meter/meterModels';
import {
  getPageError,
  getPageIsFetching,
  getPageResult,
  getPaginatedEntities,
} from '../state/domain-models-paginated/paginatedDomainModelsSelectors';
import {isSuperAdmin} from '../state/domain-models/user/userSelectors';
import {addAllToReport, addToReport} from '../state/report/reportActions';
import {changePage} from '../state/ui/pagination/paginationActions';
import {EntityTypes, Pagination} from '../state/ui/pagination/paginationModels';
import {getPagination} from '../state/ui/pagination/paginationSelectors';
import {getPaginatedMeterParameters} from '../state/user-selection/userSelectionSelectors';
import {syncMeters, syncWithMetering} from '../usecases/meter/meterActions';

const mapStateToProps = (
  {
    auth: {user},
    userSelection: {userSelection},
    paginatedDomainModels: {meters},
    ui: {pagination: paginationModel},
    search: {validation: {query}},
  }: RootState,
): MeterListStateToProps => {
  const entityType: EntityTypes = 'meters';
  const pagination: Pagination = getPagination({entityType, pagination: paginationModel});
  const {page} = pagination;
  const {sort} = meters;

  return ({
    entityType,
    entities: getPaginatedEntities<Meter>(meters),
    error: getPageError<Meter>(meters, page),
    result: getPageResult<Meter>(meters, page),
    parameters: getPaginatedMeterParameters({sort, pagination, userSelection, query}),
    isFetching: getPageIsFetching(meters, page),
    isSuperAdmin: isSuperAdmin(user!),
    pagination,
    sort,
  });
};

const mapDispatchToProps = (dispatch: Dispatch): MeterListDispatchToProps => bindActionCreators({
  deleteMeter,
  addAllToReport,
  addToReport,
  syncWithMetering,
  syncMeters,
  fetchMeters,
  changePage,
  clearError: clearErrorMeters,
  sortTable: sortTableMeters,
}, dispatch);

export const MeterListContainer = connect(mapStateToProps, mapDispatchToProps)(MeterListContent);

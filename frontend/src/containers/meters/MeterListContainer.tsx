import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {
  MeterListContent,
  MeterListDispatchToProps,
  MeterListStateToProps
} from '../../components/meters/MeterListContent';
import {RootState} from '../../reducers/rootReducer';
import {isSelectionPage} from '../../selectors/routerSelectors';
import {clearErrorMeters, deleteMeter, fetchMeters} from '../../state/domain-models-paginated/meter/meterApiActions';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
import {sortTable} from '../../state/domain-models-paginated/paginatedDomainModelsActions';
import {
  getPageError,
  getPageIsFetching,
  getPageResult,
  getPaginatedEntities,
} from '../../state/domain-models-paginated/paginatedDomainModelsSelectors';
import {isSuperAdmin} from '../../state/domain-models/user/userSelectors';
import {changePage} from '../../state/ui/pagination/paginationActions';
import {EntityTypes, Pagination} from '../../state/ui/pagination/paginationModels';
import {getPagination} from '../../state/ui/pagination/paginationSelectors';
import {getPaginatedMeterParameters} from '../../state/user-selection/userSelectionSelectors';
import {ComponentId} from '../../types/Types';
import {selectEntryAdd, showMetersInGraph} from '../../usecases/report/reportActions';
import {syncMeters, syncWithMetering} from '../../usecases/meter/meterActions';

const mapStateToProps = (
  {
    auth: {user},
    userSelection: {userSelection},
    paginatedDomainModels: {meters},
    routing,
    ui: {pagination: paginationModel},
    search: {validation: {query}},
  }: RootState,
  {componentId}: ComponentId,
): MeterListStateToProps => {
  const entityType: EntityTypes = 'meters';
  const pagination: Pagination = getPagination({componentId, entityType, pagination: paginationModel});
  const {page} = pagination;
  const {sort} = meters;

  return ({
    entities: getPaginatedEntities<Meter>(meters),
    result: getPageResult(meters, page),
    parameters: getPaginatedMeterParameters({
      sort,
      pagination,
      userSelection,
      query: isSelectionPage(routing) ? undefined : query,
    }),
    sort,
    isFetching: getPageIsFetching(meters, page),
    isSuperAdmin: isSuperAdmin(user!),
    pagination,
    error: getPageError<Meter>(meters, page),
    entityType,
    page,
  });
};

const mapDispatchToProps = (dispatch): MeterListDispatchToProps => bindActionCreators({
  deleteMeter,
  selectEntryAdd,
  syncWithMetering,
  syncMeters,
  showMetersInGraph,
  fetchMeters,
  changePage,
  clearError: clearErrorMeters,
  sortTable,
}, dispatch);

export const MeterListContainer =
  connect<MeterListStateToProps, MeterListDispatchToProps, ComponentId>(
    mapStateToProps,
    mapDispatchToProps,
  )(MeterListContent);

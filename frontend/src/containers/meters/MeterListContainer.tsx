import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {
  MeterListContent,
  MeterListDispatchToProps,
  MeterListStateToProps
} from '../../components/meters/MeterListContent';
import {RootState} from '../../reducers/rootReducer';
import {isSelectionPage} from '../../selectors/routerSelectors';
import {clearErrorMeters, fetchMeters} from '../../state/domain-models-paginated/meter/meterApiActions';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
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

  return ({
    entities: getPaginatedEntities<Meter>(meters),
    result: getPageResult(meters, page),
    parameters: getPaginatedMeterParameters({
      pagination,
      userSelection,
      query: isSelectionPage(routing) ? undefined : query,
    }),
    isFetching: getPageIsFetching(meters, page),
    isSuperAdmin: isSuperAdmin(user!),
    pagination,
    error: getPageError<Meter>(meters, page),
    entityType,
  });
};

const mapDispatchToProps = (dispatch): MeterListDispatchToProps => bindActionCreators({
  selectEntryAdd,
  syncWithMetering,
  syncMeters,
  showMetersInGraph,
  fetchMeters,
  changePage,
  clearError: clearErrorMeters,
}, dispatch);

export const MeterListContainer =
  connect<MeterListStateToProps, MeterListDispatchToProps, ComponentId>(
    mapStateToProps,
    mapDispatchToProps,
  )(MeterListContent);

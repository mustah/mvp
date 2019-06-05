import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {DispatchToProps, MeterGrid, StateToProps} from '../components/meters/MeterGrid';
import {RootState} from '../reducers/rootReducer';
import {
  deleteMeter,
  fetchMeters,
  sortTableMeters as sortTable
} from '../state/domain-models-paginated/meter/meterApiActions';
import {getAllMeters, getPageIsFetching} from '../state/domain-models-paginated/paginatedDomainModelsSelectors';
import {addToReport} from '../state/report/reportActions';
import {changePage} from '../state/ui/pagination/paginationActions';
import {EntityTypes, Pagination} from '../state/ui/pagination/paginationModels';
import {getPagination} from '../state/ui/pagination/paginationSelectors';
import {getPaginatedMeterParameters} from '../state/user-selection/userSelectionSelectors';
import {OwnProps} from '../usecases/meter/components/MeterList';
import {syncWithMetering} from '../usecases/meter/meterActions';

const mapStateToProps = ({
  meterDetail: {selectedMeterId},
  paginatedDomainModels: {meters},
  search: {validation: {query}},
  summary: {payload: {numMeters}},
  ui: {pagination: paginationModel},
  userSelection: {userSelection}
}: RootState): StateToProps => {
  const entityType: EntityTypes = 'meters';
  const pagination: Pagination = getPagination({entityType, pagination: paginationModel});
  const {page, totalElements} = pagination;
  const {sort} = meters;
  const isFetching = getPageIsFetching(meters, page);
  const allMeters = getAllMeters(meters);
  return ({
    isFetching,
    hasContent: isFetching || totalElements > 0 || numMeters > 0,
    meters: allMeters,
    pagination,
    parameters: getPaginatedMeterParameters({sort, pagination, userSelection, query}),
    selectedMeterId,
    sortOptions: sort,
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  addToReport,
  changePage,
  deleteMeter,
  fetchMeters,
  sortTable,
  syncWithMetering,
}, dispatch);

export const MeterGridContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(MeterGrid);

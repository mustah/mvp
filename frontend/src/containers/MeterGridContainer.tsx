import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {DispatchToProps, MeterGrid, StateToProps} from '../components/meters/MeterGrid';
import {RootState} from '../reducers/rootReducer';
import {deleteMeter, fetchMeters} from '../state/domain-models-paginated/meter/meterApiActions';
import {sortMeters} from '../state/domain-models-paginated/paginatedDomainModelsActions';
import {getAllMeters, getPageIsFetching} from '../state/domain-models-paginated/paginatedDomainModelsSelectors';
import {addToReport, fetchLegendItems} from '../state/report/reportActions';
import {changePage} from '../state/ui/pagination/paginationActions';
import {Pagination} from '../state/ui/pagination/paginationModels';
import {getPaginatedMeterParameters} from '../state/user-selection/userSelectionSelectors';
import {syncWithMetering} from '../usecases/meter/meterActions';
import {OwnProps} from '../usecases/meter/meterModels';

const mapStateToProps = ({
  meterDetail: {selectedMeterId},
  paginatedDomainModels: {meters},
  search: {validation: {query}},
  summary: {payload: {numMeters}},
  ui: {pagination: paginationState},
  userSelection: {userSelection}
}: RootState): StateToProps => {
  const pagination: Pagination = paginationState.meters;
  const {page, totalElements} = pagination;
  const {sort} = meters;
  const isFetching = getPageIsFetching(meters, page);

  return ({
    isFetching,
    hasContent: isFetching || totalElements > 0 || numMeters > 0,
    items: getAllMeters(meters),
    pagination,
    parameters: getPaginatedMeterParameters({sort, pagination, userSelection, query}),
    selectedItemId: selectedMeterId,
    sort,
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  addToReport,
  changePage,
  deleteMeter,
  fetchLegendItems,
  fetchMeters,
  sortTable: sortMeters,
  syncWithMetering,
}, dispatch);

export const MeterGridContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(MeterGrid);

import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {MeterDispatchToProps, MeterGrid} from '../components/meters/MeterGrid';
import {RootState} from '../reducers/rootReducer';
import {deleteMeter, fetchMeters} from '../state/domain-models-paginated/meter/meterApiActions';
import {sortMeters} from '../state/domain-models-paginated/paginatedDomainModelsActions';
import {getAllMeters, getPageIsFetching} from '../state/domain-models-paginated/paginatedDomainModelsSelectors';
import {addToReport, fetchLegendItems, limit} from '../state/report/reportActions';
import {changePage} from '../state/ui/pagination/paginationActions';
import {Pagination} from '../state/ui/pagination/paginationModels';
import {getPaginatedMeterParameters} from '../state/user-selection/userSelectionSelectors';
import {MeterListProps} from '../usecases/meter/components/MeterList';
import {syncWithMetering} from '../usecases/meter/meterActions';
import {OwnProps} from '../usecases/meter/meterModels';

const mapStateToProps = ({
  meterDetail: {selectedMeterId},
  paginatedDomainModels: {meters},
  search: {validation: {query}},
  ui: {pagination: paginationState},
  userSelection: {userSelection}
}: RootState): MeterListProps => {
  const pagination: Pagination = paginationState.meters;
  const {page, totalElements} = pagination;
  const {sort} = meters;
  const isFetching = getPageIsFetching(meters, page);

  return ({
    entityType: 'meters',
    isFetching,
    hasContent: isFetching || totalElements > 0,
    items: getAllMeters(meters),
    pagination,
    parameters: getPaginatedMeterParameters({sort, pagination, userSelection, query}),
    legendItemsParameters: getPaginatedMeterParameters({limit, sort, pagination, userSelection, query}),
    selectedItemId: selectedMeterId,
    sort,
  });
};

const mapDispatchToProps = (dispatch): MeterDispatchToProps => bindActionCreators({
  addToReport,
  changePage,
  deleteMeter,
  fetchLegendItems,
  fetchPaginated: fetchMeters,
  sortTable: sortMeters,
  syncWithMetering,
}, dispatch);

export const MeterGridContainer =
  connect<MeterListProps, MeterDispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(MeterGrid);

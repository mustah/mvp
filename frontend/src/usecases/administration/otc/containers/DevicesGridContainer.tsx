import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {DispatchToProps, StateToProps} from '../../../../components/infinite-list/InfiniteList';
import {RootState} from '../../../../reducers/rootReducer';
import {fetchDevices} from '../../../../state/domain-models-paginated/devices/deviceApiActions';
import {Device} from '../../../../state/domain-models-paginated/devices/deviceModels';
import {sortDevices} from '../../../../state/domain-models-paginated/paginatedDomainModelsActions';
import {getDevices, getPageIsFetching} from '../../../../state/domain-models-paginated/paginatedDomainModelsSelectors';
import {changePage} from '../../../../state/ui/pagination/paginationActions';
import {Pagination} from '../../../../state/ui/pagination/paginationModels';
import {getDevicesParameters} from '../../../../state/user-selection/userSelectionSelectors';
import {DevicesGrid} from '../components/DevicesGrid';

const mapStateToProps = (rootState: RootState): StateToProps<Device> => {
  const {
    paginatedDomainModels: {devices},
    ui: {pagination: paginationState},
  }: RootState = rootState;

  const pagination: Pagination = paginationState.devices;
  const {page, totalElements} = pagination;
  const {sort} = devices;
  const isFetching = getPageIsFetching(devices, page);

  return ({
    entityType: 'devices',
    hasContent: isFetching || totalElements > 0,
    isFetching,
    items: getDevices(devices),
    parameters: getDevicesParameters(rootState),
    pagination,
    sort,
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changePage,
  fetchPaginated: fetchDevices,
  sortTable: sortDevices,
}, dispatch);

export const DevicesGridContainer = connect(mapStateToProps, mapDispatchToProps)(DevicesGrid);

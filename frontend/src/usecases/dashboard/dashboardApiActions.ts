import {GetState} from '../../reducers/rootReducer';
import {EndPoints} from '../../services/endPoints';
import {fetchIfNeeded, FetchIfNeeded} from '../../state/api/apiActions';
import {DashboardModel} from './dashboardModels';
import {DashboardState} from './dashboardReducer';

const shouldFetchDashboard: FetchIfNeeded = (getState: GetState): boolean => {
  const dashboard: DashboardState = getState().dashboard;
  return !dashboard.isSuccessfullyFetched && !dashboard.error && !dashboard.isFetching;
};

export const fetchDashboard = fetchIfNeeded<DashboardModel>(
  EndPoints.dashboard,
  shouldFetchDashboard,
);

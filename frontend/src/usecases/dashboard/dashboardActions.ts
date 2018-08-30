import {GetState} from '../../reducers/rootReducer';
import {EndPoints} from '../../services/endPoints';
import {fetchIfNeeded, FetchIfNeeded} from '../../state/api/apiActions';
import {uuid} from '../../types/Types';
import {centerMap} from '../map/mapActions';
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

export const centerMapOnMeter = (id: uuid) =>
  (dispatch, getState: GetState) => {
    const geoPosition = getState().domainModels.meterMapMarkers.entities[id];
    if (geoPosition) {
      dispatch(centerMap(geoPosition));
    }
  };

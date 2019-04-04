import {Dispatch} from 'react-redux';
import {GetState, RootState} from '../../../reducers/rootReducer';
import {EndPoints} from '../../../services/endPoints';
import {firstUpperTranslated} from '../../../services/translationService';
import {ErrorResponse, uuid} from '../../../types/Types';
import {centerMap} from '../../../usecases/map/mapActions';
import {MapMarker} from '../../../usecases/map/mapModels';
import {showFailMessage} from '../../ui/message/messageActions';
import {fetchIfNeeded, putRequest} from '../domainModelsActions';
import {Dashboard} from './dashboardModels';
import {dashboardDataFormatter} from './dashboardSchema';

export const updateDashboard = putRequest<Dashboard, Dashboard>(EndPoints.dashboard, {
  afterFailure: ({message}: ErrorResponse, dispatch: Dispatch<RootState>) => {
    dispatch(showFailMessage(firstUpperTranslated(
      'failed to update dashboard: {{error}}',
      {error: message},
    )));
  },
});

export const fetchDashboards = fetchIfNeeded<Dashboard>(
  EndPoints.dashboard,
  'dashboards',
  dashboardDataFormatter,
);

export const centerMapOnMeter = (id: uuid) =>
  (dispatch, getState: GetState) => {
    const geoPosition: MapMarker = getState().domainModels.meterMapMarkers.entities[id];
    if (geoPosition) {
      const {latitude, longitude} = geoPosition;
      dispatch(centerMap({latitude, longitude}));
    }
  };

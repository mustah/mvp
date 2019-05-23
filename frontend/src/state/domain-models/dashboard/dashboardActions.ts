import {EndPoints} from '../../../services/endPoints';
import {firstUpperTranslated} from '../../../services/translationService';
import {Dispatch, ErrorResponse} from '../../../types/Types';
import {showFailMessage} from '../../ui/message/messageActions';
import {fetchIfNeeded, putRequest} from '../domainModelsActions';
import {Dashboard} from './dashboardModels';
import {dashboardDataFormatter} from './dashboardSchema';

export const updateDashboard = putRequest<Dashboard, Dashboard>(EndPoints.dashboard, {
  afterFailure: ({message}: ErrorResponse, dispatch: Dispatch) => {
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

import {Dashboard as DashboardModel} from '../../state/domain-models/dashboard/dashboardModels';
import {WidgetState} from '../../state/widget/widgetReducer';
import {EncodedUriParameters, uuid} from '../../types/Types';

export const getMeterCount = (state: WidgetState, id: uuid): number => {
  if (state[id] !== undefined && state[id].data !== undefined) {
    return state[id].data || 0;
  }
  return 0;
};

export const makeWidgetParametersOf = (dashboard: DashboardModel): EncodedUriParameters =>
  dashboard ? `dashboardId=${dashboard.id}` : '';

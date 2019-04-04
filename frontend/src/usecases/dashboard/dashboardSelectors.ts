import {Dashboard, Dashboard as DashboardModel} from '../../state/domain-models/dashboard/dashboardModels';
import {NormalizedState} from '../../state/domain-models/domainModels';
import {getFirstDomainModel} from '../../state/domain-models/domainModelsSelectors';
import {WidgetState} from '../../state/widget/widgetReducer';
import {uuid} from '../../types/Types';

export const getMeterCount = (state: WidgetState, id: uuid): number => {
  if (state[id] !== undefined && state[id].data !== undefined) {
    return state[id].data || 0;
  }
  return 0;
};

export const hasLayout = (dashboard: DashboardModel) => !(!dashboard.layout.layout);

export const hasLayoutItems = (dashboard: DashboardModel) => dashboard.layout.layout.length > 0;

export const hasDashboardWidgets = (state: NormalizedState<Dashboard>): boolean =>
  getFirstDomainModel(state)
    .filter(hasLayoutItems)
    .isJust();

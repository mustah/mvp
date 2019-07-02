import {isDefined} from '../../helpers/commonHelpers';
import {Maybe} from '../../helpers/Maybe';
import {Dashboard, Dashboard as DashboardModel} from '../../state/domain-models/dashboard/dashboardModels';
import {NormalizedState} from '../../state/domain-models/domainModels';
import {getFirstDomainModel} from '../../state/domain-models/domainModelsSelectors';
import {WidgetModel} from '../../state/widget/widgetReducer';

export const isFetching = (widgetModel?: WidgetModel): boolean =>
  Maybe.maybe(widgetModel)
    .map(it => it.isFetching)
    .orElse(false);

export const getMeterCount = (widgetModel?: WidgetModel): number =>
  Maybe.maybe(widgetModel)
    .map(it => it.data)
    .filter(isDefined)
    .orElse(0);

export const hasLayout = (dashboard: DashboardModel) => !(!dashboard.layout.layout);

export const hasLayoutItems = (dashboard: DashboardModel) => dashboard.layout.layout.length > 0;

export const hasDashboardWidgets = (state: NormalizedState<Dashboard>): boolean =>
  getFirstDomainModel(state)
    .filter(hasLayoutItems)
    .isJust();

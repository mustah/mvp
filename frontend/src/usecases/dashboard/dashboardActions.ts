import {idGenerator} from '../../helpers/idGenerator';
import {GetState} from '../../reducers/rootReducer';
import {EndPoints} from '../../services/endPoints';
import {restClient} from '../../services/restClient';
import {fetchDashboards} from '../../state/domain-models/dashboard/dashboardActions';
import {Dashboard, Dashboard as DashboardModel} from '../../state/domain-models/dashboard/dashboardModels';
import {dashboardDataFormatter} from '../../state/domain-models/dashboard/dashboardSchema';
import {Normalized} from '../../state/domain-models/domainModels';
import {getRequestOf} from '../../state/domain-models/domainModelsActions';
import {getFirstDomainModel} from '../../state/domain-models/domainModelsSelectors';
import {fetchWidgets} from '../../state/domain-models/widget/widgetActions';
import {WidgetType} from '../../state/domain-models/widget/widgetModels';
import {makeCollectionWidget, makeMapWidget, widgetDimensions} from './dashboardHelpers';
import {hasLayout} from './dashboardSelectors';

const makeInitialDashboard = (): DashboardModel => {
  const collectionProps = widgetDimensions[WidgetType.COLLECTION];
  const mapProps = widgetDimensions[WidgetType.MAP];
  const collectionWidgetId = idGenerator.uuid().toString();
  const mapWidgetId = idGenerator.uuid().toString();
  return ({
    id: idGenerator.uuid(),
    layout: {
      layout: [
        {i: collectionWidgetId, x: mapProps.w + 1, y: 0, w: collectionProps.w, h: collectionProps.h},
        {i: mapWidgetId, x: 0, y: 0, w: mapProps.w, h: mapProps.h},
      ]
    }
  });
};

const dashboardRequestHandler = getRequestOf<Normalized<Dashboard>>(EndPoints.dashboard);

const saveInitialDashboard = async (dispatch): Promise<void> => {
  try {
    const {data: dashboard} = await restClient.post(EndPoints.dashboard, makeInitialDashboard());
    await restClient.post(EndPoints.widgets, makeCollectionWidget(dashboard.layout.layout[0].i!, dashboard.id));
    await restClient.post(EndPoints.widgets, makeMapWidget(dashboard.layout.layout[1].i!, dashboard.id));
    dispatch(dashboardRequestHandler.success(dashboardDataFormatter([dashboard])));
  } catch (e) {
    dispatch(dashboardRequestHandler.failure(e));
  }
};

export const onFetchDashboards = () =>
  async (dispatch, getState: GetState) => {
    const {domainModels: {dashboards}} = getState();

    dispatch(fetchDashboards());

    if (dashboards.isSuccessfullyFetched) {
      const dashboard = getFirstDomainModel(dashboards).filter(hasLayout);
      if (dashboard.isNothing()) {
        await saveInitialDashboard(dispatch);
      } else {
        dashboard.do(({id}) => dispatch(fetchWidgets(`dashboardId=${id}`)));
      }
    }
  };

import {lifecycle} from 'recompose';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history3/redirect';
import {Dashboard} from '../../state/domain-models/dashboard/dashboardModels';
import {DomainModel, NormalizedState} from '../../state/domain-models/domainModels';
import {Widget} from '../../state/domain-models/widget/widgetModels';
import {CallbackWithData, EncodedUriParameters, Fetch} from '../../types/Types';
import {MapMarker} from '../map/mapModels';

export interface DashboardStateToProps {
  widgets: NormalizedState<Widget>;
  dashboard?: Dashboard;
  isFetching: boolean;
  isSuccessfullyFetched: boolean;
  meterMapMarkers: DomainModel<MapMarker>;
  parameters: EncodedUriParameters;
}

export interface DispatchToProps {
  fetchDashboard: Fetch;
  fetchWidgets: Fetch;
  addWidgetToDashboard: CallbackWithData;
  updateWidget: CallbackWithData;
  updateDashboard: CallbackWithData;
  addDashboard: CallbackWithData;
  deleteWidget: CallbackWithData;
}

export type DashboardProps = DashboardStateToProps & DispatchToProps & InjectedAuthRouterProps;

export const withNewDashboardDataFetchers = lifecycle<DashboardProps, {}, {}>({

  componentDidMount() {
    const {fetchDashboard, fetchWidgets, parameters, dashboard} = this.props;
    fetchDashboard();
    if (dashboard) {
      fetchWidgets(parameters);
    }
  },

  componentWillReceiveProps({fetchDashboard, fetchWidgets, parameters, dashboard}: DashboardProps) {
    fetchDashboard();
    if (dashboard && dashboard.layout) {
      fetchWidgets(parameters);
    }
  }
});

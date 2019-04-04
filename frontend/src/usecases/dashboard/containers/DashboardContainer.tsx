import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {updateDashboard} from '../../../state/domain-models/dashboard/dashboardActions';
import {Dashboard as DashboardModel} from '../../../state/domain-models/dashboard/dashboardModels';
import {NormalizedState} from '../../../state/domain-models/domainModels';
import {getFirstDomainModel} from '../../../state/domain-models/domainModelsSelectors';
import {addWidgetToDashboard, deleteWidget, updateWidget} from '../../../state/domain-models/widget/widgetActions';
import {Widget} from '../../../state/domain-models/widget/widgetModels';
import {Callback, CallbackWithData} from '../../../types/Types';
import {DashboardComponent} from '../components/Dashboard';
import {onFetchDashboards} from '../dashboardActions';
import {hasDashboardWidgets} from '../dashboardSelectors';

export interface StateToProps {
  dashboard: Maybe<DashboardModel>;
  hasContent: boolean;
  isFetching: boolean;
  isSuccessfullyFetched: boolean;
  widgets: NormalizedState<Widget>;
}

export interface DispatchToProps {
  addWidgetToDashboard: CallbackWithData;
  deleteWidget: CallbackWithData;
  onFetchDashboards: Callback;
  updateWidget: CallbackWithData;
  updateDashboard: CallbackWithData;
}

const mapStateToProps = ({domainModels: {dashboards, widgets}}: RootState): StateToProps => {
  const {isFetching, isSuccessfullyFetched} = dashboards;
  const dashboard = getFirstDomainModel<DashboardModel>(dashboards);
  return ({
    dashboard,
    hasContent: hasDashboardWidgets(dashboards) && widgets.isSuccessfullyFetched,
    isFetching: (isFetching && !isSuccessfullyFetched) || dashboard.isNothing(),
    isSuccessfullyFetched,
    widgets,
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  addWidgetToDashboard,
  deleteWidget,
  onFetchDashboards,
  updateWidget,
  updateDashboard,
}, dispatch);

export const DashboardContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(DashboardComponent);

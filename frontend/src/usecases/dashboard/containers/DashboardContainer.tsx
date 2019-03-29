import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {addDashboard, fetchDashboard, updateDashboard} from '../../../state/domain-models/dashboard/dashboardActions';
import {Dashboard as DashboardModel} from '../../../state/domain-models/dashboard/dashboardModels';
import {
  addWidgetToDashboard,
  deleteWidget,
  fetchWidgets,
  updateWidget
} from '../../../state/domain-models/widget/widgetActions';
import {EncodedUriParameters} from '../../../types/Types';
import {Dashboard} from '../components/Dashboard';
import {DashboardStateToProps, DispatchToProps, withDashboardDataFetchers} from '../dashboardEnhancers';

const makeWidgetParametersOf =
  (dashboard: DashboardModel): EncodedUriParameters =>
    'dashboardId=' + dashboard.id;

const mapStateToProps =
  ({
    domainModels: {
      meterMapMarkers, dashboards: {result, entities, isFetching, isSuccessfullyFetched}, widgets
    },
  }: RootState): DashboardStateToProps =>
    ({
      widgets,
      dashboard: entities[result[0]],
      meterMapMarkers,
      isFetching,
      isSuccessfullyFetched,
      parameters: entities[result[0]] && makeWidgetParametersOf(entities[result[0]]),
    });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchDashboard,
  fetchWidgets,
  addWidgetToDashboard,
  updateWidget,
  updateDashboard,
  addDashboard,
  deleteWidget,
}, dispatch);

export const DashboardContainer = connect<DashboardStateToProps, DispatchToProps>(
  mapStateToProps,
  mapDispatchToProps,
)(withDashboardDataFetchers(Dashboard));

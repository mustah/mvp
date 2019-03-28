import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {addDashboard, fetchDashboard, updateDashboard} from '../../../state/domain-models/dashboard/dashboardActions';
import {Dashboard} from '../../../state/domain-models/dashboard/dashboardModels';
import {
  addWidgetToDashboard,
  deleteWidget,
  fetchWidgets,
  updateWidget
} from '../../../state/domain-models/widget/widgetActions';
import {EncodedUriParameters} from '../../../types/Types';
import {NewDashboard} from '../components/NewDashboard';
import {DashboardStateToProps, DispatchToProps, withNewDashboardDataFetchers} from '../dashboardEnhancers';

const makeWidgetParametersOf =
  (dashboard: Dashboard): EncodedUriParameters =>
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

export const NewDashboardContainer = connect<DashboardStateToProps, DispatchToProps>(
  mapStateToProps,
  mapDispatchToProps,
)(withNewDashboardDataFetchers(NewDashboard));

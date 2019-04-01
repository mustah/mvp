import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {addDashboard, fetchDashboard, updateDashboard} from '../../../state/domain-models/dashboard/dashboardActions';
import {Dashboard as DashboardModel} from '../../../state/domain-models/dashboard/dashboardModels';
import {DomainModel, NormalizedState} from '../../../state/domain-models/domainModels';
import {
  addWidgetToDashboard,
  deleteWidget,
  fetchWidgets,
  updateWidget
} from '../../../state/domain-models/widget/widgetActions';
import {Widget} from '../../../state/domain-models/widget/widgetModels';
import {CallbackWithData, EncodedUriParameters, Fetch} from '../../../types/Types';
import {MapMarker} from '../../map/mapModels';
import {Dashboard} from '../components/Dashboard';
import {makeWidgetParametersOf} from '../dashboardSelectors';

export interface StateToProps {
  widgets: NormalizedState<Widget>;
  dashboard?: DashboardModel;
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

const mapStateToProps =
  ({
    domainModels: {
      meterMapMarkers,
      dashboards: {result, entities, isSuccessfullyFetched},
      widgets
    },
  }: RootState): StateToProps => {
    const dashboard = entities[result[0]];
    return ({
      dashboard,
      isSuccessfullyFetched,
      meterMapMarkers,
      parameters: makeWidgetParametersOf(dashboard),
      widgets,
    });
  };

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchDashboard,
  fetchWidgets,
  addWidgetToDashboard,
  updateWidget,
  updateDashboard,
  addDashboard,
  deleteWidget,
}, dispatch);

export const DashboardContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(Dashboard);

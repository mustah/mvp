import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {RootState} from '../../../reducers/rootReducer';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {getDomainModel} from '../../../state/domain-models/domainModelsSelectors';
import {allCurrentMeterParameters} from '../../../state/user-selection/userSelectionSelectors';
import {EncodedUriParameters, Fetch} from '../../../types/Types';
import {fetchMeterMapMarkers} from '../../map/mapMarkerActions';
import {MapMarker} from '../../map/mapModels';
import {Dashboard} from '../components/Dashboard';
import {fetchDashboard} from '../dashboardActions';
import {withDashboardDataFetchers} from '../dashboardEnhancers';
import {DashboardModel} from '../dashboardModels';

interface StateToProps {
  dashboard?: DashboardModel;
  isFetching: boolean;
  meterMapMarkers: DomainModel<MapMarker>;
  parameters: EncodedUriParameters;
}

interface DispatchToProps {
  fetchDashboard: Fetch;
  fetchMeterMapMarkers: Fetch;
}

export type DashboardProps = StateToProps & DispatchToProps & InjectedAuthRouterProps;

const mapStateToProps =
  ({
    dashboard: {record: dashboard, isFetching},
    domainModels: {meterMapMarkers},
  }: RootState): StateToProps =>
    ({
      dashboard,
      isFetching,
      parameters: allCurrentMeterParameters,
      meterMapMarkers: getDomainModel(meterMapMarkers),
    });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchDashboard,
  fetchMeterMapMarkers,
}, dispatch);

export const DashboardContainer = connect<StateToProps, DispatchToProps>(
  mapStateToProps,
  mapDispatchToProps,
)(withDashboardDataFetchers(Dashboard));

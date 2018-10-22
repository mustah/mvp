import {lifecycle} from 'recompose';
import {DashboardProps} from './containers/DashboardContainer';

export const withDashboardDataFetchers = lifecycle<DashboardProps, {}, {}>({

  componentDidMount() {
    const {fetchDashboard, fetchMeterMapMarkers, parameters} = this.props;
    fetchDashboard(parameters);
    fetchMeterMapMarkers(parameters);
  },

  componentWillReceiveProps({fetchDashboard, fetchMeterMapMarkers, parameters}: DashboardProps) {
    fetchDashboard(parameters);
    fetchMeterMapMarkers(parameters);
  }
});

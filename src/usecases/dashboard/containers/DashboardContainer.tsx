import * as React from 'react';
import {connect} from 'react-redux';
import {withRouter} from 'react-router';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/index';
import {SelectionsOverview} from '../components/SelectionsOverview';
import {fetchDashboard} from '../dashboardActions';
import {DashboardProps} from '../dashboardReducer';

export interface DashboardContainerProps {
  fetchDashboard: () => any;
  dashboard: DashboardProps;
}

const DashboardContainer: React.StatelessComponent<DashboardContainerProps> = (props) => {
  const {fetchDashboard} = props;
  return (
    <div>
      <SelectionsOverview title={'Allt'}/>

      <div className="button" onClick={fetchDashboard}>DASHBOARD</div>
    </div>
  );
};

const mapStateToProps = (state: RootState) => {
  const {dashboard} = state;
  return {
    dashboard,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  fetchDashboard,
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(withRouter(DashboardContainer));

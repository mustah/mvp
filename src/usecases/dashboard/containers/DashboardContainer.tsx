import * as React from 'react';
import {connect} from 'react-redux';
import {withRouter} from 'react-router';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/index';
import {SelectionsOverview} from '../components/SelectionsOverview';
import {fetchDashboard} from '../dashboardActions';

export interface DashboardContainerProps {
  fetchDashboard: any;
}

class DashboardContainer extends React.Component<DashboardContainerProps | any, any> {
  render() {
    const {fetchDashboard} = this.props;
    return (
      <div>
        <SelectionsOverview/>

        <div className="button" onClick={fetchDashboard}>DASHBOARD</div>
      </div>
    );
  }
}

const mapStateToProps = (state: RootState) => ({...state});

const mapDispatchToProps = dispatch => {
  return bindActionCreators({
    fetchDashboard,
  }, dispatch);
};

export default connect(mapStateToProps, mapDispatchToProps)(withRouter(DashboardContainer));

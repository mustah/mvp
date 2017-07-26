import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/index';
import {Column} from '../../layouts/components/column/Column';
import {Layout} from '../../layouts/components/layout/Layout';
import {SelectionsOverview} from '../components/SelectionsOverview';
import {fetchDashboards} from '../dashboardActions';
import {DashboardState} from '../dashboardReducer';

export interface DashboardContainerProps {
  fetchDashboards: () => any;
  dashboard: DashboardState;
}

const DashboardContainer = (props: DashboardContainerProps) => {
  const {fetchDashboards} = props;
  return (
    <Layout>
      <Column className="flex-1">
        <SelectionsOverview title={'Allt'}/>
        <div className="button" onClick={fetchDashboards}>DASHBOARD</div>
      </Column>
    </Layout>

  );
};

const mapStateToProps = (state: RootState) => {
  const {dashboard} = state;
  return {
    dashboard,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  fetchDashboards,
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(DashboardContainer);

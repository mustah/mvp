import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/index';
import {SelectionOverview} from '../../common/components/selectionoverview/SelectionOverview';
import {Bold, Large} from '../../common/components/texts/Texts';
import {Column} from '../../layouts/components/column/Column';
import {Layout} from '../../layouts/components/layout/Layout';
import {Map} from '../components/map/Map';
import {fetchDashboards} from '../dashboardActions';
import {DashboardState} from '../dashboardReducer';
import {SystemOverviewContainer} from './SystemOverviewContainer';

export interface DashboardContainerProps {
  fetchDashboards: () => any;
  dashboard: DashboardState;
}

const DashboardContainer = (props: DashboardContainerProps) => {
  const {fetchDashboards} = props;
  return (
    <Layout>
      <Column className="flex-1">
        <SelectionOverview title={'Allt'}/>
        <SystemOverviewContainer/>
        <Large><Bold>Bestånd</Bold></Large>
        <Map/>
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

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
  const {fetchDashboards, dashboard} = props;
  const now = new Date();

  return (
    <Layout>
      <Column className="flex-1">
        <SelectionOverview title={'Allt'}/>
        <SystemOverviewContainer/>
        <Large><Bold>Bestånd</Bold></Large>
        <Map/>
        <h3>
          <div className="button" onClick={fetchDashboards}>
            Click me to load dashboard data from json-server via Rest!!!
          </div>
        </h3>
        <div>
          {dashboard.records.map((record, index) => (<li key={record.id}>{record.title}</li>))}
        </div>
        <div>
          <h3>Updated: {now.toLocaleString()} </h3>
        </div>
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

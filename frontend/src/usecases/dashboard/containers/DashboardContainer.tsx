import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {RootState} from '../../../reducers/index';
import {translate} from '../../../services/translationService';
import {SelectionOverview} from '../../common/components/selectionoverview/SelectionOverview';
import {Xlarge} from '../../common/components/texts/Texts';
import {Column} from '../../layouts/components/column/Column';
import {Content} from '../../layouts/components/content/Content';
import {Layout} from '../../layouts/components/layout/Layout';
import {SystemOverviewContainer} from '../../systemOverview/containers/SystemOverviewContainer';
import {MeteringPoint} from '../../table/components/meteringPoint/MeteringPoint';
import {StatusIcon} from '../../table/components/statusIcon/StatusIcon';
import {Table} from '../../table/components/table/Table';
import {TableHead} from '../../table/components/table/TableHead';
import {TableColumn} from '../../table/components/tableColumn/TableColumn';
import {Map} from '../components/map/Map';
import {fetchDashboard} from '../dashboardActions';
import {DashboardState} from '../dashboardReducer';

export interface DashboardContainerProps {
  fetchDashboard: () => any;
  dashboard: DashboardState;
}

class DashboardContainer extends React.Component<DashboardContainerProps & InjectedAuthRouterProps, any> {
  componentDidMount() {
    if (this.props.isAuthenticated) {
      this.props.fetchDashboard();
    }
  }

  render() {
    const {fetchDashboard, dashboard} = this.props;
    const now = new Date();

    // this format is what we can expect from https://github.com/paularmstrong/normalizr
    const normalizedData = {
      meteringPoints: {
        byId: {
          '1234 1234 1234': {
            id: '1234 1234 1234',
            type: 'UNICOcoder',
            location: 'Område 1 fast 12',
            gateway: 'YY',
            status: {
              code: 0,
              text: 'ok',
            },
          },
          '1234 1234 1235': {
            id: '1234 1234 1235',
            type: 'UNICOcoder',
            location: 'Område 1 fast 12',
            gateway: 'YY',
            status: {
              code: 2,
              text: 'Mätare går baklänges',
            },
          },
          '1234 1234 1236': {
            id: '1234 1234 1236',
            type: 'UNICOcoder',
            location: 'Område 1 fast 12',
            gateway: 'YY',
            status: {
              code: 3,
              text: 'Mätare går inte alls',
            },
          },
        },
        allIds: ['1234 1234 1234', '1234 1234 1235', '1234 1234 1236'],
      },
    };

    const renderMeteringPointCell = (value, index) => <MeteringPoint id={value}/>;
    const renderStatusCell = (value, index) => <StatusIcon code={value.code} content={value.text}/>;

    return (
      <Layout>
        <Column className="flex-1">
          <SelectionOverview title={'Allt'}/>
          <Content>
            {dashboard.record && <SystemOverviewContainer overview={dashboard.record.systemOverview}/>}
            <Xlarge className="Bold">Bestånd</Xlarge>
            <Map/>
            <Table data={normalizedData.meteringPoints}>
              <TableColumn
                id={'id'}
                header={<TableHead>{translate('meter')}</TableHead>}
                cell={renderMeteringPointCell}
              />
              <TableColumn
                id={'type'}
                header={<TableHead>{translate('type')}</TableHead>}
              />
              <TableColumn
                id={'location'}
                header={<TableHead>{translate('location')}</TableHead>}
              />
              <TableColumn
                id={'gateway'}
                header={<TableHead>{translate('gateway')}</TableHead>}
              />
              <TableColumn
                id={'status'}
                header={<TableHead sortable={true} currentSort={'asc'}>{translate('status')}</TableHead>}
                cell={renderStatusCell}
              />
            </Table>
            <h3>
              <div className="button" onClick={fetchDashboard}>
                Click me to load dashboard data from json-server via Rest!!!
              </div>
            </h3>
            <div>
              <h3>Updated: {now.toLocaleString()} </h3>
            </div>
          </Content>
        </Column>
      </Layout>
    );
  }
}

/**
 * React deals with both state and props, but when we introduce
 * Redux, Redux takes over the ownership of state.
 *
 * Changing the state of a React component is called to "dispatch"
 * in Redux.
 *
 * @param {RootState} state
 * @returns {{dashboard: DashboardState}}
 */
const mapStateToProps = (state: RootState) => {
  const {dashboard} = state;
  return {
    dashboard,
  };
};

/**
 * Handle both triggering of and listening to events in the DashboardContainer
 *
 * @param dispatch
 */
const mapDispatchToProps = dispatch => bindActionCreators({
  fetchDashboard,
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(DashboardContainer);

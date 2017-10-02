import * as React from 'react';
import {connect} from 'react-redux';
import {Link} from 'react-router-dom';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {RootState} from '../../../reducers/index';
import {SelectionOverview} from '../../common/components/selectionoverview/SelectionOverview';
import {Xlarge} from '../../common/components/texts/Texts';
import {Column} from '../../layouts/components/column/Column';
import {Content} from '../../layouts/components/content/Content';
import {Layout} from '../../layouts/components/layout/Layout';
import {SystemOverviewContainer} from '../../systemOverview/containers/SystemOverviewContainer';
import {Table, TableColumn} from '../../table/components/Table';
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

    // TODO both the columnset and the rows should come from the backend
    // Perhaps only the column metadata should come from the backend,
    // and we should have a fixed set of formatting callbacks, that
    // matches the name of the column in the backend/in the context?
    // i.e. having a generic "metering_point": function.., map
    const columns: TableColumn[] = [
      {
        index: 'meter',
        formatted: 'Meter',
        renderCell: function (value, index) {
          return (<Link to={'/meter/' + value}>
            {value}
          </Link>);
        }
      },
      {
        index: 'type',
        formatted: 'Type',
      },
      {
        index: 'location',
        formatted: 'Location',
      },
      {
        index: 'gateway',
        formatted: 'Gateway',
      },
      {
        index: 'status',
        formatted: 'Status',
      },
      {
        index: 'statusCode',
      },
    ];

    const rows = [
      [
        '1234 1234 1234',
        'UNICOcoder',
        'Område 1 fast 12',
        'YY',
        'OK',
      ],
      [
        '1234 1234 1234',
        'UNICOcoder',
        'Område 1 fast 12',
        'YY',
        'Mätare går baklänges',
      ],
      [
        '1234 1234 1234',
        'UNICOcoder',
        'Område 1 fast 12',
        'YY',
        'Mätare går baklänges',
      ],
      [
        '1234 1234 1234',
        'UNICOcoder',
        'Område 1 fast 12',
        'YY',
        'Mätare går baklänges',
      ],
      [
        '1234 1234 1234',
        'UNICOcoder',
        'Område 1 fast 12',
        'YY',
        'Mätare går baklänges',
      ],
    ];

    return (
      <Layout>
        <Column className="flex-1">
          <SelectionOverview title={'Allt'}/>
          <Content>
            {dashboard.record && <SystemOverviewContainer overview={dashboard.record.systemOverview}/>}
            <Xlarge className="Bold">Bestånd</Xlarge>
            <Map/>
            <Table columns={columns} rows={rows}/>
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

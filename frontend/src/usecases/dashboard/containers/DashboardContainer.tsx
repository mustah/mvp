import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {RootState} from '../../../reducers/index';
import {translate} from '../../../services/translationService';
import {SelectedIndicatorWidgetProps} from '../../common/components/indicators/IndicatorWidgets';
import {SelectionOverview} from '../../common/components/selection-overview/SelectionOverview';
import {Title} from '../../common/components/texts/Title';
import {Column} from '../../common/components/layouts/column/Column';
import {Content} from '../../common/components/layouts/content/Content';
import {Layout} from '../../common/components/layouts/layout/Layout';
import {selectDashboardIndicatorWidget} from '../../ui/uiActions';
import {SystemOverview} from '../components/system-overview/SystemOverview';
import {fetchDashboard} from '../dashboardActions';
import {DashboardState} from '../dashboardReducer';
import {SystemOverviewState} from '../models/dashboardModels';
import DashboardTabsContainer from './DashboardTabsContainer';

export interface DashboardContainerProps extends SelectedIndicatorWidgetProps {
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
    const {
      dashboard: {record},
      selectIndicatorWidget,
      selectedWidget,
    } = this.props;

    const renderSystemOverview = (systemOverview: SystemOverviewState) => (
      <SystemOverview
        title={systemOverview.title}
        indicators={systemOverview.indicators}
        selectedWidget={selectedWidget}
        selectIndicatorWidget={selectIndicatorWidget}
      />
    );

    return (
      <Layout>
        <Column className="flex-1">
          <SelectionOverview title={translate('all')}/>
          <Content>
            {record && renderSystemOverview(record.systemOverview)}

            <Title>{translate('collection')}</Title>

            <DashboardTabsContainer/>

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
    selectedWidget: state.ui.selectedIndicators.dashboard,
  };
};

/**
 * Handle both triggering of and listening to events in the DashboardContainer
 *
 * @param dispatch
 */
const mapDispatchToProps = dispatch => bindActionCreators({
  fetchDashboard,
  selectIndicatorWidget: selectDashboardIndicatorWidget,
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(DashboardContainer);

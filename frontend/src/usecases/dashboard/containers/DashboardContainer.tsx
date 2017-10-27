import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {PeriodSelection} from '../../common/components/dates/PeriodSelection';
import {
  IndicatorWidgetsDispatchProps,
  SelectedIndicatorWidgetProps,
} from '../../common/components/indicators/IndicatorWidgets';
import {PageContainer} from '../../common/containers/PageContainer';
import {Row} from '../../common/components/layouts/row/Row';
import {MainTitle, Title} from '../../common/components/texts/Title';
import {selectDashboardIndicatorWidget} from '../../../state/ui/indicator/indicatorActions';
import {SystemOverview} from '../components/system-overview/SystemOverview';
import {fetchDashboard} from '../dashboardActions';
import {DashboardState} from '../dashboardReducer';
import {SystemOverviewState} from '../models/dashboardModels';
import DashboardTabsContainer from './DashboardTabsContainer';

interface StateToProps extends SelectedIndicatorWidgetProps {
  dashboard: DashboardState;
}

export interface DispatchToProps extends IndicatorWidgetsDispatchProps {
  fetchDashboard: () => any;
}

class DashboardContainer extends React.Component<StateToProps & DispatchToProps & InjectedAuthRouterProps> {

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
        indicators={systemOverview.indicators}
        selectedWidget={selectedWidget}
        selectIndicatorWidget={selectIndicatorWidget}
      />
    );

    return (
      <PageContainer>
        <Row className="space-between">
          <MainTitle>{translate('dashboard')}</MainTitle>
          <PeriodSelection/>
        </Row>

        {record && renderSystemOverview(record.systemOverview)}

        <Title>{translate('collection')}</Title>
        <DashboardTabsContainer/>
      </PageContainer>
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
const mapStateToProps = (state: RootState): StateToProps => {
  const {dashboard} = state;
  return {
    dashboard,
    selectedWidget: state.ui.indicator.selectedIndicators.dashboard,
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

export default connect<StateToProps, DispatchToProps, {}>(mapStateToProps, mapDispatchToProps)(DashboardContainer);

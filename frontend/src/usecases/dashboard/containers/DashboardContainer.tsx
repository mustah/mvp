import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {selectDashboardIndicatorWidget} from '../../../state/ui/indicator/indicatorActions';
import {getSelectedIndicatorDashboard} from '../../../state/ui/indicator/indicatorSelectors';
import {
  IndicatorWidgetsDispatchProps,
  SelectedIndicatorWidgetProps,
} from '../../common/components/indicators/IndicatorWidgets';
import {Row} from '../../common/components/layouts/row/Row';
import {MainTitle} from '../../common/components/texts/Title';
import {PageContainer} from '../../common/containers/PageContainer';
import {SystemOverview} from '../components/system-overview/SystemOverview';
import {fetchDashboard} from '../dashboardActions';
import {DashboardState} from '../dashboardReducer';
import {SystemOverviewState} from '../models/dashboardModels';

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
        showSelected={false}
      />
    );

    return (
      <PageContainer>
        <Row className="space-between">
          <MainTitle>{translate('dashboard')}</MainTitle>
        </Row>

        {record && renderSystemOverview(record.systemOverview)}
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
const mapStateToProps = ({dashboard, ui}: RootState): StateToProps => {
  return {
    dashboard,
    selectedWidget: getSelectedIndicatorDashboard(ui),
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

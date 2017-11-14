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
} from '../../common/components/indicators/SelectableIndicatorWidgets';
import {Column} from '../../common/components/layouts/column/Column';
import {Row} from '../../common/components/layouts/row/Row';
import {Bold} from '../../common/components/texts/Texts';
import {MainTitle} from '../../common/components/texts/Titles';
import {PageContainer} from '../../common/containers/PageContainer';
import {PeriodContainer} from '../../common/containers/PeriodContainer';
import {SummaryContainer} from '../../common/containers/SummaryContainer';
import {OverviewWidgets} from '../components/widgets/OverviewWidgets';
import {Widget} from '../components/widgets/Widget';
import {fetchDashboard} from '../dashboardActions';
import {DashboardState} from '../dashboardReducer';
import {DashboardModel} from '../models/dashboardModels';

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
    } = this.props;

    return (
      <PageContainer>
        <Row className="space-between">
          <MainTitle>{translate('dashboard')}</MainTitle>
          <Row>
            <SummaryContainer/>
            <PeriodContainer/>
          </Row>
        </Row>

        {this.renderWidgets(record)}
      </PageContainer>
    );
  }

  renderWidgets = (records?: DashboardModel) => {
    if (records) {
      return (
        <Column>
          <OverviewWidgets widgets={records.systemOverview.widgets}/>

          <Row>
            <Widget>
              <Bold>Map TODO</Bold>
            </Widget>
            <Widget>
              <Bold>Map - 2 - TODO</Bold>
            </Widget>
          </Row>
        </Column>
      );
    }
    return null;
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

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
} from '../../../components/indicators/SelectableIndicatorWidgets';
import {Column} from '../../../components/layouts/column/Column';
import {Row} from '../../../components/layouts/row/Row';
import {MainTitle} from '../../../components/texts/Titles';
import {PageContainer} from '../../../containers/PageContainer';
import {PeriodContainer} from '../../../containers/PeriodContainer';
import {SummaryContainer} from '../../../containers/SummaryContainer';
import {OverviewWidgets} from '../components/widgets/OverviewWidgets';
import {fetchDashboard} from '../dashboardActions';
import {DashboardState} from '../dashboardReducer';
import {DashboardModel} from '../dashboardModels';
import {Meter} from '../../../state/domain-models/meter/meterModels';
import {getMeterEntities} from '../../../state/domain-models/meter/meterSelectors';
import {MapWidgets} from '../components/widgets/MapWidgets';

interface StateToProps extends SelectedIndicatorWidgetProps {
  dashboard: DashboardState;
  entities: { [key: string]: Meter };
}

interface DispatchToProps extends IndicatorWidgetsDispatchProps {
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
      entities,
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

        {this.renderWidgets(entities, record)}
      </PageContainer>
    );
  }

  renderWidgets = (entities: any, records?: DashboardModel) => {
    if (records) {
      return (
        <Column>
          <OverviewWidgets widgets={records.widgets}/>
          <MapWidgets tmp={entities}/>
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
const mapStateToProps = ({dashboard, ui, domainModels}: RootState): StateToProps => {
  const entityState = domainModels.meters;
  return {
    dashboard,
    selectedWidget: getSelectedIndicatorDashboard(ui),
    entities: getMeterEntities(entityState),
  };
};

/**
 * Handle both triggering of and listening to events in the DashboardContainer
 *
 * @param dispatch
 */
const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchDashboard,
  selectIndicatorWidget: selectDashboardIndicatorWidget,
}, dispatch);

export default connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(DashboardContainer);

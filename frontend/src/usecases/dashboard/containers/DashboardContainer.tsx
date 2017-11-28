import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {SelectedIndicatorWidgetProps} from '../../../components/indicators/SelectableIndicatorWidgets';
import {Column} from '../../../components/layouts/column/Column';
import {Row} from '../../../components/layouts/row/Row';
import {MainTitle} from '../../../components/texts/Titles';
import {PageContainer} from '../../../containers/PageContainer';
import {PeriodContainer} from '../../../containers/PeriodContainer';
import {SummaryContainer} from '../../../containers/SummaryContainer';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {Meter} from '../../../state/domain-models/meter/meterModels';
import {getMeterEntities} from '../../../state/domain-models/meter/meterSelectors';
import {OverviewWidgets} from '../components/widgets/OverviewWidgets';
import {fetchDashboard} from '../dashboardActions';
import {DashboardModel} from '../dashboardModels';
import {Loader} from '../../../components/loading/Loader';
import {MapWidgetsContainer} from '../components/widgets/MapWidgetsContainer';

interface StateToProps extends SelectedIndicatorWidgetProps {
  isFetching: boolean;
  dashboard?: DashboardModel;
  meters: DomainModel<Meter>;
}

interface DispatchToProps {
  fetchDashboard: () => any;
}

type Props = StateToProps & DispatchToProps & InjectedAuthRouterProps;

class DashboardContainerComponent extends React.Component<Props> {

  componentDidMount() {
      this.props.fetchDashboard();
    }

  render() {
    const {isFetching, dashboard, meters} = this.props;
    return (
      <PageContainer>
        <Row className="space-between">
          <MainTitle>{translate('dashboard')}</MainTitle>
          <Row>
            <SummaryContainer/>
            <PeriodContainer/>
          </Row>
        </Row>

        <Loader isFetching={isFetching}>
        <Column>
            {dashboard && <OverviewWidgets widgets={dashboard.widgets}/>}
            <MapWidgetsContainer markers={meters}/>
        </Column>
        </Loader>
      </PageContainer>
      );
    }
  }

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchDashboard,
}, dispatch);

const mapStateToProps = ({dashboard, domainModels: {meters}}: RootState): StateToProps => {
  return {
    isFetching: dashboard.isFetching,
    dashboard: dashboard.record,
    meters: getMeterEntities(meters),
  };
};

export const DashboardContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(DashboardContainerComponent);

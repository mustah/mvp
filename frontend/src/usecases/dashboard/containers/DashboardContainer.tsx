import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {Column} from '../../../components/layouts/column/Column';
import {Row} from '../../../components/layouts/row/Row';
import {Loader} from '../../../components/loading/Loader';
import {MainTitle} from '../../../components/texts/Titles';
import {MvpPageContainer} from '../../../containers/MvpPageContainer';
import {PeriodContainer} from '../../../containers/PeriodContainer';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {Meter} from '../../../state/domain-models-paginated/meter/meterModels';
import {getPaginatedEntities} from '../../../state/domain-models-paginated/paginatedDomainModelsSelectors';
import {ObjectsById} from '../../../state/domain-models/domainModels';
import {Callback} from '../../../types/Types';
import {OverviewWidgets} from '../components/widgets/OverviewWidgets';
import {fetchDashboard} from '../dashboardActions';
import {DashboardModel} from '../dashboardModels';

interface StateToProps {
  isFetching: boolean;
  dashboard?: DashboardModel;
  meters: ObjectsById<Meter>;
}

interface DispatchToProps {
  fetchDashboard: Callback;
}

type Props = StateToProps & DispatchToProps & InjectedAuthRouterProps;

class DashboardContainerComponent extends React.Component<Props> {

  componentDidMount() {
    this.props.fetchDashboard();
  }

  render() {
    const {isFetching, dashboard} = this.props;
    return (
      <MvpPageContainer>
        <Row className="space-between">
          <MainTitle>{translate('dashboard')}</MainTitle>
          <Row>
            {/*<SummaryContainer/>*/}
            <PeriodContainer/>
          </Row>
        </Row>

        <Loader isFetching={isFetching}>
          <Column>
            {dashboard && <OverviewWidgets widgets={dashboard.widgets}/>}
            {/*<MapWidgetsContainer markers={meters}/>*/}
          </Column>
        </Loader>
      </MvpPageContainer>
    );
  }
}

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchDashboard,
}, dispatch);

const mapStateToProps = ({dashboard, paginatedDomainModels: {meters}}: RootState): StateToProps => {
  return {
    isFetching: dashboard.isFetching,
    dashboard: dashboard.record,
    meters: getPaginatedEntities(meters),
  };
};

export const DashboardContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(DashboardContainerComponent);

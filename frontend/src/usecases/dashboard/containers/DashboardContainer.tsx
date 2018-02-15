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
import {SummaryContainer} from '../../../containers/SummaryContainer';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {Meter} from '../../../state/domain-models-paginated/meter/meterModels';
import {ClearError, ObjectsById} from '../../../state/domain-models/domainModels';
import {clearErrorMetersAll} from '../../../state/domain-models/domainModelsActions';
import {getEntitiesDomainModels, getError} from '../../../state/domain-models/domainModelsSelectors';
import {Callback, ErrorResponse} from '../../../types/Types';
import {MapWidgetsContainer} from '../components/widgets/MapWidgetsContainer';
import {OverviewWidgets} from '../components/widgets/OverviewWidgets';
import {fetchDashboard} from '../dashboardActions';
import {DashboardModel} from '../dashboardModels';

interface StateToProps {
  dashboard?: DashboardModel;
  meters: ObjectsById<Meter>;
  isFetching: boolean;
  error: Maybe<ErrorResponse>;
}

interface DispatchToProps {
  fetchDashboard: Callback;
  clearError: ClearError;
}

type Props = StateToProps & DispatchToProps & InjectedAuthRouterProps;

class DashboardContainerComponent extends React.Component<Props> {

  componentDidMount() {
    this.props.fetchDashboard();
  }

  render() {
    const {isFetching, dashboard, meters, error, clearError} = this.props;
    return (
      <MvpPageContainer>
        <Row className="space-between">
          <MainTitle>{translate('dashboard')}</MainTitle>
          <Row>
            <SummaryContainer/>
            <PeriodContainer/>
          </Row>
        </Row>

        <Loader isFetching={isFetching} error={error} clearError={clearError}>
          <Column>
            {dashboard && <OverviewWidgets widgets={dashboard.widgets}/>}
            <MapWidgetsContainer markers={meters}/>
          </Column>
        </Loader>
      </MvpPageContainer>
    );
  }
}

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchDashboard,
  clearError: clearErrorMetersAll,
}, dispatch);

const mapStateToProps = ({dashboard, domainModels: {metersAll}}: RootState): StateToProps => {
  return {
    dashboard: dashboard.record,
    meters: getEntitiesDomainModels(metersAll),
    isFetching: dashboard.isFetching,
    error: getError(metersAll),
  };
};

export const DashboardContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(DashboardContainerComponent);

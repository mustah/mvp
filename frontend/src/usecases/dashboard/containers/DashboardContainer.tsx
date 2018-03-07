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
import {ObjectsById} from '../../../state/domain-models/domainModels';
import {clearErrorAllMeters, fetchAllMeters} from '../../../state/domain-models/meter-all/allMetersApiActions';
import {getEntitiesDomainModels, getError} from '../../../state/domain-models/domainModelsSelectors';
import {getEncodedUriParametersForAllMeters} from '../../../state/search/selection/selectionSelectors';
import {Callback, ClearError, ErrorResponse, RestGet} from '../../../types/Types';
import {MapWidgetsContainer} from '../components/widgets/MapWidgetsContainer';
import {OverviewWidgets} from '../components/widgets/OverviewWidgets';
import {fetchDashboard} from '../dashboardActions';
import {DashboardModel} from '../dashboardModels';

interface StateToProps {
  dashboard?: DashboardModel;
  meters: ObjectsById<Meter>;
  isFetching: boolean;
  error: Maybe<ErrorResponse>;
  encodedUriParametersForAllMeters: string;
}

interface DispatchToProps {
  fetchDashboard: Callback;
  clearError: ClearError;
  fetchAllMeters: RestGet;
}

type Props = StateToProps & DispatchToProps & InjectedAuthRouterProps;

class DashboardContainerComponent extends React.Component<Props> {

  componentDidMount() {
    const {fetchDashboard, fetchAllMeters, encodedUriParametersForAllMeters} = this.props;
    fetchDashboard();
    fetchAllMeters(encodedUriParametersForAllMeters);
  }

  componentWillReceiveProps({fetchAllMeters, encodedUriParametersForAllMeters}: Props) {
    fetchAllMeters(encodedUriParametersForAllMeters);
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

const mapStateToProps = ({dashboard, searchParameters, domainModels: {allMeters}}: RootState): StateToProps => ({
  dashboard: dashboard.record,
  meters: getEntitiesDomainModels(allMeters),
  isFetching: dashboard.isFetching,
  error: getError(allMeters),
  encodedUriParametersForAllMeters: getEncodedUriParametersForAllMeters(searchParameters),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchDashboard,
  clearError: clearErrorAllMeters,
  fetchAllMeters,
}, dispatch);

export const DashboardContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(DashboardContainerComponent);

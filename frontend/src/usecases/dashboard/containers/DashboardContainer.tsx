import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {Row} from '../../../components/layouts/row/Row';
import {Loader} from '../../../components/loading/Loader';
import {MainTitle} from '../../../components/texts/Titles';
import {MvpPageContainer} from '../../../containers/MvpPageContainer';
import {PeriodContainer} from '../../../containers/PeriodContainer';
import {SummaryContainer} from '../../../containers/SummaryContainer';
import {now} from '../../../helpers/dateHelpers';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {getDomainModel, getError} from '../../../state/domain-models/domainModelsSelectors';
import {getMeterParameters} from '../../../state/user-selection/userSelectionSelectors';
import {ClearError, EncodedUriParameters, ErrorResponse, Fetch} from '../../../types/Types';
import {MapMarker} from '../../map/mapModels';
import {clearErrorMeterMapMarkers, fetchMeterMapMarkers} from '../../map/meterMapMarkerApiActions';
import {OverviewWidgets} from '../components/widgets/OverviewWidgets';
import {fetchDashboard} from '../dashboardApiActions';
import {DashboardModel} from '../dashboardModels';
import {MapWidgetContainer} from './MapWidgetContainer';

interface StateToProps {
  dashboard?: DashboardModel;
  meterMapMarkers: DomainModel<MapMarker>;
  isFetching: boolean;
  error: Maybe<ErrorResponse>;
  parameters: EncodedUriParameters;
}

interface DispatchToProps {
  clearError: ClearError;
  fetchDashboard: Fetch;
  fetchMeterMapMarkers: Fetch;
}

type Props = StateToProps & DispatchToProps & InjectedAuthRouterProps;

class DashboardContainerComponent extends React.Component<Props> {

  componentDidMount() {
    const {fetchDashboard, fetchMeterMapMarkers, parameters} = this.props;
    fetchDashboard(parameters);
    fetchMeterMapMarkers(parameters);
  }

  componentWillReceiveProps({fetchDashboard, fetchMeterMapMarkers, parameters}: Props) {
    fetchDashboard(parameters);
    fetchMeterMapMarkers(parameters);
  }

  render() {
    const {
      isFetching,
      dashboard,
      meterMapMarkers,
      error,
      clearError,
    } = this.props;
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
          <Row className="Row-wrap-reverse">
            <MapWidgetContainer markers={meterMapMarkers}/>
            {dashboard && <OverviewWidgets widgets={dashboard.widgets}/>}
          </Row>
        </Loader>
      </MvpPageContainer>
    );
  }
}

const mapStateToProps =
  ({
    dashboard: {record, isFetching},
    userSelection: {userSelection},
    domainModels: {meterMapMarkers},
  }: RootState): StateToProps =>
    ({
      dashboard: record,
      parameters: getMeterParameters({userSelection, now: now()}),
      meterMapMarkers: getDomainModel(meterMapMarkers),
      isFetching: isFetching || meterMapMarkers.isFetching,
      error: getError(meterMapMarkers),
    });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchDashboard,
  clearError: clearErrorMeterMapMarkers,
  fetchMeterMapMarkers,
}, dispatch);

export const DashboardContainer = connect<StateToProps, DispatchToProps>(
  mapStateToProps,
  mapDispatchToProps,
)(DashboardContainerComponent);

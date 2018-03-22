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
import {ObjectsById} from '../../../state/domain-models/domainModels';
import {getEntitiesDomainModels, getError} from '../../../state/domain-models/domainModelsSelectors';
import {clearErrorAllMeters} from '../../../state/domain-models/meter-all/allMetersApiActions';
import {Callback, ClearError, ErrorResponse, Fetch} from '../../../types/Types';
import {MapMarker} from '../../map/mapModels';
import {fetchMeterMapMarkers} from '../../map/meterMapMarkerApiActions';
import {MapWidgetsContainer} from '../components/widgets/MapWidgetsContainer';
import {OverviewWidgets} from '../components/widgets/OverviewWidgets';
import {fetchDashboard} from '../dashboardActions';
import {DashboardModel} from '../dashboardModels';

interface StateToProps {
  dashboard?: DashboardModel;
  meterMapMarkers: ObjectsById<MapMarker>;
  isFetching: boolean;
  error: Maybe<ErrorResponse>;
}

interface DispatchToProps {
  fetchDashboard: Callback;
  clearError: ClearError;
  fetchMeterMapMarkers: Fetch;
}

type Props = StateToProps & DispatchToProps & InjectedAuthRouterProps;

class DashboardContainerComponent extends React.Component<Props> {

  componentDidMount() {
    const {fetchDashboard, fetchMeterMapMarkers} = this.props;
    fetchDashboard();
    fetchMeterMapMarkers();
  }

  componentWillReceiveProps({fetchMeterMapMarkers}: Props) {
    fetchMeterMapMarkers();
  }

  render() {
    const {isFetching, dashboard, meterMapMarkers, error, clearError} = this.props;
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
            <MapWidgetsContainer markers={meterMapMarkers}/>
          </Column>
        </Loader>
      </MvpPageContainer>
    );
  }
}

const mapStateToProps = ({dashboard, searchParameters, domainModels: {meterMapMarkers}}: RootState): StateToProps => ({
  dashboard: dashboard.record,
  meterMapMarkers: getEntitiesDomainModels(meterMapMarkers),
  isFetching: dashboard.isFetching || meterMapMarkers.isFetching,
  error: getError(meterMapMarkers),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchDashboard,
  clearError: clearErrorAllMeters,
  fetchMeterMapMarkers,
}, dispatch);

export const DashboardContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(DashboardContainerComponent);

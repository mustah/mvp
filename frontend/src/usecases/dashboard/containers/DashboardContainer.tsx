import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {WidgetModel} from '../../../components/indicators/indicatorWidgetModels';
import {Row} from '../../../components/layouts/row/Row';
import {MainTitle} from '../../../components/texts/Titles';
import {MvpPageContainer} from '../../../containers/MvpPageContainer';
import {PeriodContainer} from '../../../containers/PeriodContainer';
import {SummaryContainer} from '../../../containers/SummaryContainer';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {getDomainModel} from '../../../state/domain-models/domainModelsSelectors';
import {getMeterParameters} from '../../../state/user-selection/userSelectionSelectors';
import {EncodedUriParameters, Fetch} from '../../../types/Types';
import {fetchMeterMapMarkers} from '../../map/mapMarkerActions';
import {MapMarker} from '../../map/mapModels';
import {OverviewWidgets} from '../components/widgets/OverviewWidgets';
import {fetchDashboard} from '../dashboardActions';
import {DashboardModel} from '../dashboardModels';
import {MapWidgetContainer} from './MapWidgetContainer';

interface StateToProps {
  dashboard?: DashboardModel;
  isFetching: boolean;
  meterMapMarkers: DomainModel<MapMarker>;
  parameters: EncodedUriParameters;
}

interface DispatchToProps {
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
    const {dashboard, isFetching, meterMapMarkers} = this.props;
    const widgets: WidgetModel[] = isFetching || !dashboard ? [] : dashboard.widgets;
    return (
      <MvpPageContainer>
        <Row className="space-between">
          <MainTitle>{translate('dashboard')}</MainTitle>
          <Row>
            <SummaryContainer/>
            <PeriodContainer/>
          </Row>
        </Row>

        <Row className="Row-wrap-reverse">
          <MapWidgetContainer markers={meterMapMarkers}/>
          <OverviewWidgets widgets={widgets} isFetching={isFetching}/>
        </Row>
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
      isFetching,
      parameters: getMeterParameters({userSelection}),
      meterMapMarkers: getDomainModel(meterMapMarkers),
    });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchDashboard,
  fetchMeterMapMarkers,
}, dispatch);

export const DashboardContainer = connect<StateToProps, DispatchToProps>(
  mapStateToProps,
  mapDispatchToProps,
)(DashboardContainerComponent);

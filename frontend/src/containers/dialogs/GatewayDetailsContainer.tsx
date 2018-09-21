import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {Period} from '../../components/dates/dateModels';
import {withLargeLoader} from '../../components/hoc/withLoaders';
import {now} from '../../helpers/dateHelpers';
import {Maybe} from '../../helpers/Maybe';
import {makeApiParametersOf} from '../../helpers/urlFactory';
import {RootState} from '../../reducers/rootReducer';
import {fetchGateway} from '../../state/domain-models-paginated/gateway/gatewayApiActions';
import {Gateway} from '../../state/domain-models-paginated/gateway/gatewayModels';
import {getGatewayMeterIds} from '../../state/domain-models-paginated/gateway/gatewaySelectors';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
import {getMeterDetailsByIds} from '../../state/domain-models-paginated/meter/meterSelectors';
import {getPaginatedDomainModelById} from '../../state/domain-models-paginated/paginatedDomainModelsSelectors';
import {ObjectsById} from '../../state/domain-models/domainModels';
import {getDomainModelById} from '../../state/domain-models/domainModelsSelectors';
import {fetchMeterDetails} from '../../state/domain-models/meter-details/meterDetailsApiActions';
import {isSuperAdmin} from '../../state/domain-models/user/userSelectors';
import {CallbackWithId, CallbackWithIds, uuid} from '../../types/Types';
import {MapMarker, SelectedId} from '../../usecases/map/mapModels';
import './GatewayDetailsContainer.scss';
import {GatewayDetailsInfoContainer} from './GatewayDetailsInfo';
import {GatewayDetailsTabs} from './GatewayDetailsTabs';

interface StateToProps {
  isSuperAdmin: boolean;
  isFetching: boolean;
  gateway: Maybe<Gateway>;
  gatewayMapMarker: Maybe<MapMarker>;
  meters: Maybe<ObjectsById<Meter>>;
}

interface DispatchToProps {
  fetchGateway: CallbackWithId;
  fetchMeterDetails: CallbackWithIds;
}

type Props = StateToProps & DispatchToProps & SelectedId;

const GatewayDetailsContent = (props: Props) => {
  const {gateway, meters} = props;
  if (gateway.isNothing() || meters.isNothing()) {
    return null;
  }
  return (
    <div>
      <GatewayDetailsInfoContainer gateway={gateway.get()}/>
      <GatewayDetailsTabs {...props} gateway={gateway.get()} meters={meters.get()}/>
    </div>
  );
};

const GatewayDetailsContentLoader = withLargeLoader<Props>(GatewayDetailsContent);

const fetchGatewayAndItsMeters =
  ({fetchGateway, gateway, fetchMeterDetails, selectedId}: Props) => {
    selectedId.do((id: uuid) => fetchGateway(id));
    gateway.filter(({meterIds}: Gateway) => meterIds.length > 0)
      .map(({meterIds}: Gateway) =>
        fetchMeterDetails(meterIds, makeApiParametersOf(now(), {period: Period.latest})));
  };

class GatewayDetails extends React.Component<Props> {

  componentDidMount() {
    fetchGatewayAndItsMeters(this.props);
  }

  componentWillReceiveProps(props: Props) {
    fetchGatewayAndItsMeters(props);
  }

  render() {
    return <GatewayDetailsContentLoader {...this.props}/>;
  }
}

const mapStateToProps = (
  {
    auth: {user},
    paginatedDomainModels: {gateways},
    domainModels: {gatewayMapMarkers, meters},
  }: RootState,
  {selectedId}: SelectedId,
): StateToProps => {
  const gateway: Maybe<Gateway> = selectedId
    .flatMap((id: uuid) => getPaginatedDomainModelById<Gateway>(id)(gateways));
  return ({
    gateway,
    meters: getMeterDetailsByIds(getGatewayMeterIds(gateway))(meters),
    gatewayMapMarker: selectedId
      .flatMap((id: uuid) => getDomainModelById<MapMarker>(id)(gatewayMapMarkers)),
    isFetching: gateways.isFetchingSingle || meters.isFetching,
    isSuperAdmin: isSuperAdmin(user!),
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchGateway,
  fetchMeterDetails,
}, dispatch);

export const GatewayDetailsContainer =
  connect<StateToProps, DispatchToProps, SelectedId>(
    () => mapStateToProps,
    mapDispatchToProps,
  )(GatewayDetails);

import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {withLargeLoader} from '../../components/hoc/withLargeLoader';
import {Maybe} from '../../helpers/Maybe';
import {RootState} from '../../reducers/rootReducer';
import {fetchGateway} from '../../state/domain-models-paginated/gateway/gatewayApiActions';
import {Gateway} from '../../state/domain-models-paginated/gateway/gatewayModels';
import {getGatewayMeterIdsFrom} from '../../state/domain-models-paginated/gateway/gatewaySelectors';
import {fetchMeterEntities} from '../../state/domain-models-paginated/meter/meterApiActions';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
import {getMetersByIds} from '../../state/domain-models-paginated/meter/meterSelectors';
import {getPaginatedDomainModelById} from '../../state/domain-models-paginated/paginatedDomainModelsSelectors';
import {ObjectsById} from '../../state/domain-models/domainModels';
import {getDomainModelById} from '../../state/domain-models/domainModelsSelectors';
import {CallbackWithId, uuid} from '../../types/Types';
import {MapMarker} from '../../usecases/map/mapModels';
import './GatewayDetailsContainer.scss';
import {GatewayDetailsInfoContainer} from './GatewayDetailsInfo';
import {GatewayDetailsTabs} from './GatewayDetailsTabs';

interface OwnProps {
  gatewayId: uuid;
}

interface DispatchToProps {
  fetchGateway: CallbackWithId;
  fetchMeterEntities: (ids: uuid[], size?: number) => void;
}

interface StateToProps {
  isFetching: boolean;
  gateway: Maybe<Gateway>;
  gatewayMapMarker: Maybe<MapMarker>;
  meters: Maybe<ObjectsById<Meter>>;
}

type Props = OwnProps & StateToProps & DispatchToProps;

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
  ({fetchGateway, gatewayId, gateway, fetchMeterEntities}: Props) => {
    fetchGateway(gatewayId);
    gateway
      .filter(({meterIds}: Gateway) => meterIds.length > 0)
      .map(({meterIds}: Gateway) => {
        fetchMeterEntities(meterIds, meterIds.length);
      });
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
  {paginatedDomainModels: {meters, gateways}, domainModels: {gatewayMapMarkers}}: RootState,
  {gatewayId}: OwnProps,
): StateToProps => {
  const gateway: Maybe<Gateway> = getPaginatedDomainModelById<Gateway>(gatewayId)(gateways);
  return ({
    gateway,
    meters: getMetersByIds(getGatewayMeterIdsFrom(gateway))(meters),
    gatewayMapMarker: getDomainModelById<MapMarker>(gatewayId)(gatewayMapMarkers),
    isFetching: gateways.isFetchingSingle || meters.isFetchingSingle,
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchGateway,
  fetchMeterEntities,
}, dispatch);

export const GatewayDetailsContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(
    () => mapStateToProps,
    mapDispatchToProps,
  )(GatewayDetails);

import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {Maybe} from '../../helpers/Maybe';
import {RootState} from '../../reducers/rootReducer';
import {fetchGateway} from '../../state/domain-models-paginated/gateway/gatewayApiActions';
import {Gateway} from '../../state/domain-models-paginated/gateway/gatewayModels';
import {getGateway} from '../../state/domain-models-paginated/gateway/gatewaySelectors';
import {fetchMeterEntities} from '../../state/domain-models-paginated/meter/meterApiActions';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
import {getMetersByGateway} from '../../state/domain-models-paginated/meter/meterSelectors';
import {ObjectsById} from '../../state/domain-models/domainModels';
import {FetchSingle, uuid} from '../../types/Types';
import {MapMarker} from '../../usecases/map/mapModels';
import {getMapMarker} from '../../usecases/map/mapSelectors';
import './GatewayDetailsContainer.scss';
import {GatewayDetailsInfo} from './GatewayDetailsInfo';
import {GatewayDetailsTabs} from './GatewayDetailsTabs';

interface OwnProps {
  gatewayId: uuid;
}

interface DispatchToProps {
  fetchGateway: FetchSingle;
  fetchMeterEntities: (ids: uuid[]) => void;
}

interface StateToProps {
  gateway: Maybe<Gateway>;
  gatewayMapMarker: Maybe<MapMarker>;
  meters: Maybe<ObjectsById<Meter>>;
}

type Props = OwnProps & StateToProps & DispatchToProps;

class GatewayDetails extends React.Component<Props> {

  componentDidMount() {
    const {fetchGateway, gatewayId, gateway, fetchMeterEntities} = this.props;
    fetchGateway(gatewayId);
    if (gateway.isJust()) {
      fetchMeterEntities(gateway.get().meterIds);
    }
  }

  componentWillReceiveProps({fetchGateway, gatewayId, gateway, fetchMeterEntities}: Props) {
    fetchGateway(gatewayId);
    if (gateway.isJust()) {
      fetchMeterEntities(gateway.get().meterIds);
    }
  }

  render() {
    const {gateway, meters} = this.props;
    if (gateway.isJust() && meters.isJust()) {
      const newProps = {
        ...this.props,
        gateway: gateway.get(),
        meters: meters.get(),
      };
      return (
        <div>
          <GatewayDetailsInfo gateway={this.props.gateway.get()}/>
          <GatewayDetailsTabs {...newProps}/>
        </div>);
    } else {
      return <div/>;
    }
  }
}

const mapStateToProps = (
  {paginatedDomainModels: {meters, gateways}, domainModels: {gatewayMapMarkers}}: RootState,
  {gatewayId}: OwnProps,
): StateToProps => {
  const gateway = getGateway(gateways, gatewayId);
  return ({
    meters: getMetersByGateway(meters, gateway),
    gateway,
    gatewayMapMarker: getMapMarker(gatewayMapMarkers, gatewayId),
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchGateway,
  fetchMeterEntities,
}, dispatch);

export const GatewayDetailsContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(GatewayDetails);

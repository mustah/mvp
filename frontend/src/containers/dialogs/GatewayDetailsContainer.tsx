import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {Period} from '../../components/dates/dateModels';
import {withLargeLoader} from '../../components/hoc/withLoaders';
import {Column} from '../../components/layouts/column/Column';
import {TimestampInfoMessage} from '../../components/timestamp-info-message/TimestampInfoMessage';
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
import {fetchGatewayMeterDetails} from '../../state/domain-models/meter-details/meterDetailsApiActions';
import {isSuperAdmin} from '../../state/domain-models/user/userSelectors';
import {CallbackWithId, uuid} from '../../types/Types';
import {onCenterMap} from '../../usecases/map/mapActions';
import {MapMarker, OnCenterMapEvent, SelectedId} from '../../usecases/map/mapModels';
import {OnFetchGatewayMeterDetails} from './fetchDialogDataHook';
import './GatewayDetailsContainer.scss';
import {GatewayDetailsInfoContainer} from './GatewayDetailsInfoContainer';
import {GatewayDetailsTabs} from './GatewayDetailsTabs';

interface StateToProps {
  isSuperAdmin: boolean;
  isFetching: boolean;
  gateway: Maybe<Gateway>;
  gatewayMapMarker: Maybe<MapMarker>;
  meters: Maybe<ObjectsById<Meter>>;
}

interface DispatchToProps extends OnCenterMapEvent {
  fetchGateway: CallbackWithId;
  fetchGatewayMeterDetails: OnFetchGatewayMeterDetails;
}

type Props = StateToProps & DispatchToProps & SelectedId;

const GatewayDetailsContent = (props: Props) => {
  const {gateway, meters} = props;
  if (gateway.isNothing() || meters.isNothing()) {
    return null;
  }
  return (
    <Column>
      <GatewayDetailsInfoContainer gateway={gateway.get()}/>
      <GatewayDetailsTabs {...props} gateway={gateway.get()} meters={meters.get()}/>
      <TimestampInfoMessage/>
    </Column>
  );
};

const GatewayDetailsContentLoader = withLargeLoader<Props>(GatewayDetailsContent);

const GatewayDetails = (props: Props) => {
  const {fetchGateway, fetchGatewayMeterDetails, gateway, selectedId} = props;
  React.useEffect(() => {
    selectedId.do((id: uuid) => fetchGateway(id));
    gateway.filter(({meterIds}: Gateway) => meterIds.length > 0)
      .map(({id, meterIds}: Gateway) =>
        fetchGatewayMeterDetails(meterIds, makeApiParametersOf({period: Period.latest}), id));
  });

  return <GatewayDetailsContentLoader {...props}/>;
};

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
  fetchGatewayMeterDetails,
  onCenterMap,
}, dispatch);

export const GatewayDetailsContainer =
  connect<StateToProps, DispatchToProps, SelectedId>(
    () => mapStateToProps,
    mapDispatchToProps,
  )(GatewayDetails);

import * as React from 'react';
import {Period} from '../../components/dates/dateModels';
import {Maybe} from '../../helpers/Maybe';
import {makeApiParametersOf} from '../../helpers/urlFactory';
import {Gateway} from '../../state/domain-models-paginated/gateway/gatewayModels';
import {User} from '../../state/domain-models/user/userModels';
import {isSuperAdmin} from '../../state/domain-models/user/userSelectors';
import {SelectionInterval} from '../../state/user-selection/userSelectionModels';
import {CallbackWithId, CallbackWithIds, EncodedUriParameters, uuid} from '../../types/Types';

interface Props {
  fetchOrganisation: CallbackWithId;
  organisationId: uuid;
  user: User;
}

export type OnFetchGatewayMeterDetails =
  (meterIds: uuid[], parameters: EncodedUriParameters, gatewayId?: uuid) => void;

interface FetchGatewayProps {
  fetchGateway: CallbackWithId;
  fetchGatewayMeterDetails: OnFetchGatewayMeterDetails;
  gateway: Maybe<Gateway>;
  selectedId: Maybe<uuid>;
}

interface FetchMeterAndMapMarker {
  fetchMeterDetails: CallbackWithIds;
  fetchMeterMapMarker: CallbackWithId;
  selectedId: Maybe<uuid>;
  periodDateRange: SelectionInterval;
}

export const useFetchOrganisation = ({fetchOrganisation, organisationId, user}: Props): void => {
  React.useEffect(() => {
    if (isSuperAdmin(user)) {
      fetchOrganisation(organisationId);
    }
  }, [organisationId, user]);
};

export const useFetchGatewayAndItsMeters = ({
  fetchGateway,
  fetchGatewayMeterDetails,
  gateway,
  selectedId
}: FetchGatewayProps) => {
  React.useEffect(() => {
    selectedId.do((id: uuid) => fetchGateway(id));
    gateway.filter(({meterIds}: Gateway) => meterIds.length > 0)
      .map(({id, meterIds}: Gateway) =>
        fetchGatewayMeterDetails(meterIds, makeApiParametersOf({period: Period.latest}), id));
  });
};

export const useFetchMeterAndMapMarker = ({
  periodDateRange,
  fetchMeterDetails,
  fetchMeterMapMarker,
  selectedId
}: FetchMeterAndMapMarker) => {
  React.useEffect(() => {
    selectedId.do((id: uuid) => {
      fetchMeterDetails([id], makeApiParametersOf(periodDateRange));
      fetchMeterMapMarker(id);
    });
  });
};

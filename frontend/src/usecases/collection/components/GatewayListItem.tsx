import * as React from 'react';
import {OpenDialogInfoButton} from '../../../components/dialog/OpenDialogInfoButton';
import {GatewayDetailsContainer} from '../../../containers/dialogs/GatewayDetailsContainer';
import {Gateway} from '../../../state/domain-models-paginated/gateway/gatewayModels';

interface Props {
  gateway: Gateway;
}

export const GatewayListItem = ({gateway}: Props) => (
  <OpenDialogInfoButton label={gateway.serial} autoScrollBodyContent={false}>
    <GatewayDetailsContainer gatewayId={gateway.id}/>
  </OpenDialogInfoButton>
);

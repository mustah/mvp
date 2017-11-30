import * as React from 'react';
import {OpenDialogInfoButton} from '../../../components/dialog/OpenDialogInfoButton';
import {GatewayDetailsContainer} from '../../../containers/dialogs/GatewayDetailsContainer';
import {Gateway as GatewayModel} from '../../../state/domain-models/gateway/gatewayModels';

interface Props {
  gateway: GatewayModel;
}

export const GatewayListItem = (props: Props) => {
  const {gateway} = props;
  return (
    <OpenDialogInfoButton label={gateway.id}>
      <GatewayDetailsContainer gateway={gateway}/>
    </OpenDialogInfoButton>
  );
};

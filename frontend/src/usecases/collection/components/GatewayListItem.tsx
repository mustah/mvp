import * as React from 'react';
import {OpenDialogInfoButton} from '../../../components/dialog/OpenDialogInfoButton';
import {GatewayDetailsContainer} from '../../../containers/dialogs/GatewayDetailsContainer';
import {Gateway} from '../../../state/domain-models/gateway/gatewayModels';

interface Props {
  gateway: Gateway;
}

export const GatewayListItem = (props: Props) => {
  const {gateway} = props;
  return (
    <OpenDialogInfoButton label={gateway.serial}>
      <GatewayDetailsContainer gateway={gateway}/>
    </OpenDialogInfoButton>
  );
};

import * as React from 'react';
import {OpenDialogInfoButton} from '../../../components/dialog/OpenDialogInfoButton';
import {GatewayDetailsContainer} from '../../../containers/dialogs/GatewayDetailsContainer';
import {Maybe} from '../../../helpers/Maybe';
import {Gateway} from '../../../state/domain-models-paginated/gateway/gatewayModels';
import {uuid} from '../../../types/Types';

interface Props {
  gateway: Gateway;
}

export const GatewayListItem = ({gateway}: Props) => {
  const selectedId = Maybe.just<uuid>(gateway.id);
  return (
    <OpenDialogInfoButton label={gateway.serial} autoScrollBodyContent={false}>
      <GatewayDetailsContainer selectedId={selectedId}/>
    </OpenDialogInfoButton>
  );
};

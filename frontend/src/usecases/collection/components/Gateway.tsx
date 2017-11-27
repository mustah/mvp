import * as React from 'react';
import {ButtonInfoLink} from '../../../components/buttons/ButtonInfoLink';
import {GatewayDialogContainer} from '../../../containers/dialogs/GatewayDialogContainer';
import {Gateway as GatewayModel} from '../../../state/domain-models/gateway/gatewayModels';

interface GatewayProps {
  gateway: GatewayModel;
}

interface GatewayState {
  displayDialog: boolean;
}

export class Gateway extends React.Component<GatewayProps, GatewayState> {

  constructor(props) {
    super(props);
    this.state = {displayDialog: false};
  }

  render() {
    const {gateway} = this.props;
    const {displayDialog} = this.state;

    const dialog = displayDialog && (
      <GatewayDialogContainer
        gateway={gateway}
        displayDialog={displayDialog}
        close={this.close}
      />);

    return (
      <div>
        <ButtonInfoLink onClick={this.open} label={gateway.id}/>
        {dialog}
      </div>
    );
  }

  open = (event: any): void => {
    event.preventDefault();
    this.setState({displayDialog: true});
  }

  close = (): void => this.setState({displayDialog: false});

}

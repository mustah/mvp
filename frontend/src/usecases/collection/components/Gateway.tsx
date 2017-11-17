import * as React from 'react';
import {ButtonInfoLink} from '../../common/components/buttons/ButtonInfoLink';
import {GatewayDialog} from '../../common/components/dialogs/GatewayDialog';
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

    return (
      <div>
        <ButtonInfoLink onClick={this.open} label={gateway.id}/>
        <GatewayDialog gateway={gateway} displayDialog={displayDialog} close={this.close}/>
      </div>
    );
  }

  open = (event: any): void => {
    event.preventDefault();
    this.setState({displayDialog: true});
  }

  close = (): void => this.setState({displayDialog: false});

}

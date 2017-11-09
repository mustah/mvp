import 'Gateway.scss';
import * as React from 'react';
import {InfoLink} from '../common/components/buttons/InfoLink';
import {GatewayDialog} from './GatewayDialog';

interface GatewayProps {
  id: string;
}

interface GatewayState {
  displayDialog: boolean;
}

export class Gateway extends React.Component<GatewayProps, GatewayState> {

  constructor(props) {
    super(props);

    this.state = {
      displayDialog: false,
    };
  }

  render() {
    const {id} = this.props;
    const {displayDialog} = this.state;

    const open = (event: any): void => {
      event.preventDefault();
      this.setState({displayDialog: true});
    };

    const close = (): void => this.setState({displayDialog: false});

    return (
      <div>
        <InfoLink onClick={open} label={id}/>
        <GatewayDialog displayDialog={displayDialog} close={close}/>
      </div>
    );
  }
}

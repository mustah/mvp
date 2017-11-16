import * as React from 'react';
import {ButtonInfoLink} from '../buttons/ButtonInfoLink';
import {MeteringPointDialog} from '../dialogs/MeteringPointDialog';
import {uuid} from '../../../../types/Types';

export interface MeteringPointProps {
  id: uuid;
}

interface MeteringPointState {
  displayDialog: boolean;
}

export class MeteringPoint extends React.Component<MeteringPointProps, MeteringPointState> {

  constructor(props) {
    super(props);
    this.state = {displayDialog: false};
  }

  render() {
    const {id} = this.props;
    const {displayDialog} = this.state;

    return (
      <div>
        <ButtonInfoLink onClick={this.open} label={id}/>
        <MeteringPointDialog displayDialog={displayDialog} close={this.close}/>
      </div>
    );
  }

  open = (event: any): void => {
    event.preventDefault();
    this.setState({displayDialog: true});
  }

  close = (): void => this.setState({displayDialog: false});
}

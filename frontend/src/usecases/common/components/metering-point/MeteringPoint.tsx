import * as React from 'react';
import {ButtonInfoLink} from '../buttons/ButtonInfoLink';
import {MeteringPointDialogContainer} from '../../containers/dialogs/MeteringPointDialogContainer';
import {Meter} from '../../../../state/domain-models/meter/meterModels';

export interface MeteringPointProps {
  meter: Meter;
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
    const {meter} = this.props;
    const {displayDialog} = this.state;

    return (
      <div>
        <ButtonInfoLink onClick={this.open} label={meter.facility}/>
        <MeteringPointDialogContainer meter={meter} displayDialog={displayDialog} close={this.close}/>
      </div>
    );
  }

  open = (event: any): void => {
    event.preventDefault();
    this.setState({displayDialog: true});
  }

  close = (): void => this.setState({displayDialog: false});
}

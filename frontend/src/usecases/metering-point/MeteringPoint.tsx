
import 'MeteringPoint.scss';
import * as React from 'react';
import {InfoLink} from '../common/components/buttons/InfoLink';
import {MeteringPointDialog} from './MeteringPointDialog';

export interface MeteringPointProps {
  id: string;
}

interface MeteringPointState {
  displayDialog: boolean;
}

export class MeteringPoint extends React.Component<MeteringPointProps, MeteringPointState> {

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

    const close = (): void => {
      this.setState({displayDialog: false});
    };

    return (
      <div>
        <InfoLink onClick={open} label={id}/>
        <MeteringPointDialog displayDialog={displayDialog} close={close}/>
      </div>
    );
  }
}

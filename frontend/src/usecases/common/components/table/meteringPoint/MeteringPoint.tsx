import {FlatButton} from 'material-ui';
import Dialog from 'material-ui/Dialog';
import * as React from 'react';
import {translate} from '../../../../../services/translationService';

interface MeteringPointProps {
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

    const open = (event: any) => {
      event.preventDefault();
      this.setState((current) => ({...current, displayDialog: true}));
      return false;
    };

    const close = () => {
      this.setState((current) => ({...current, displayDialog: false}));
      return true;
    };

    const actions = [
      (
        <FlatButton
          label={translate('close')}
          primary={true}
          onClick={close}
        />
      ),
    ];

    return (
      <div>
        <a href={'/#/meter/' + id} onClick={open}>{id}</a>
        <Dialog
          actions={actions}
          open={this.state.displayDialog}
          onRequestClose={close}
        >
          hej hej
        </Dialog>
      </div>
    );
  }
}

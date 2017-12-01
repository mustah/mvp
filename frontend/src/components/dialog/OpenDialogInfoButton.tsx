import * as React from 'react';
import {Children} from '../../types/Types';
import {ButtonInfoLink} from '../buttons/ButtonInfoLink';
import {Dialog} from './Dialog';

interface Props {
  label: string;
  children: Children;
}

interface State {
  isOpen: boolean;
}

export class OpenDialogInfoButton extends React.Component<Props, State> {

  constructor(props) {
    super(props);
    this.state = {isOpen: false};
  }

  render() {
    const {label} = this.props;
    return (
      <div>
        <ButtonInfoLink onClick={this.open} label={label}/>
        {this.renderDialog()}
      </div>
    );
  }

  renderDialog = (): Children => {
    const {isOpen} = this.state;

    return isOpen && (
      <Dialog isOpen={isOpen} close={this.close}>
        {this.props.children}
      </Dialog>
    );
  }

  open = (event: any): void => {
    event.preventDefault();
    this.setState({isOpen: true});
  }

  close = (): void => this.setState({isOpen: false});
}

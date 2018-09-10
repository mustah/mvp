import * as React from 'react';
import {Children} from '../../types/Types';
import {ButtonInfoLink} from '../buttons/ButtonInfoLink';
import {Dialog} from './Dialog';

interface Props {
  label: string | number;
  children: Children;
  autoScrollBodyContent: boolean;
  labelStyle?: React.CSSProperties;
  iconStyle?: React.CSSProperties;
}

interface State {
  isOpen: boolean;
}

const infoLabelStyle: React.CSSProperties = {paddingLeft: 0};

export class OpenDialogInfoButton extends React.Component<Props, State> {

  state: State = {isOpen: false};

  render() {
    const {iconStyle, label, labelStyle} = this.props;
    return (
      <div>
        <ButtonInfoLink
          onClick={this.open}
          label={label}
          iconStyle={iconStyle}
          labelStyle={labelStyle || infoLabelStyle}
        />
        {this.renderDialog()}
      </div>
    );
  }

  renderDialog = (): Children => {
    const {isOpen} = this.state;
    const {autoScrollBodyContent, children} = this.props;

    return isOpen && (
      <Dialog isOpen={isOpen} close={this.close} autoScrollBodyContent={autoScrollBodyContent}>
        {children}
      </Dialog>
    );
  }

  open = (): void => this.setState({isOpen: true});

  close = (): void => this.setState({isOpen: false});
}

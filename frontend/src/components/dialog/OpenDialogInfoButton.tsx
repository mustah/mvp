import * as React from 'react';
import {Children} from '../../types/Types';
import {ButtonInfoLink} from '../buttons/ButtonInfoLink';
import {ButtonClose} from '../buttons/DialogButtons';
import {InfoButtonProps} from '../buttons/InfoButton';
import {Dialog} from './Dialog';

interface Props extends InfoButtonProps {
  autoScrollBodyContent: boolean;
  children: Children;
  label: string | number;
  labelStyle?: React.CSSProperties;
}

interface State {
  isOpen: boolean;
}

const infoLabelStyle: React.CSSProperties = {paddingLeft: 0};

export class OpenDialogInfoButton extends React.Component<Props, State> {

  state: State = {isOpen: false};

  render() {
    const {color, iconStyle, label, labelStyle} = this.props;
    return (
      <div>
        <ButtonInfoLink
          onClick={this.open}
          label={label}
          color={color}
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
        <ButtonClose onClick={this.close}/>
        {children}
      </Dialog>
    );
  }

  open = (): void => this.setState({isOpen: true});

  close = (): void => this.setState({isOpen: false});
}

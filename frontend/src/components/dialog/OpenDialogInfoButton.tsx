import * as React from 'react';
import {Children, OnClick} from '../../types/Types';
import {ButtonInfoLink} from '../buttons/ButtonInfoLink';
import {ButtonClose} from '../buttons/DialogButtons';
import {InfoButtonProps} from '../buttons/InfoButton';
import {Dialog} from './Dialog';

interface Props extends InfoButtonProps {
  autoScrollBodyContent: boolean;
  children: Children;
  label: string | number;
  labelStyle?: React.CSSProperties;
  title?: string;
  onLabelClick?: OnClick;
}

interface State {
  isOpen: boolean;
}

const infoLabelStyle: React.CSSProperties = {paddingLeft: 0};

export class OpenDialogInfoButton extends React.Component<Props, State> {

  state: State = {isOpen: false};

  render() {
    const {color, iconStyle, label, labelStyle, onLabelClick, title} = this.props;
    return (
      <div title={title}>
        <ButtonInfoLink
          label={label}
          color={color}
          iconStyle={iconStyle}
          labelStyle={labelStyle || infoLabelStyle}
          onClick={this.open}
          onLabelClick={onLabelClick}
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

  open = (event: React.SyntheticEvent<{}>): void => {
    event.stopPropagation();
    this.setState({isOpen: true});
  }

  close = (): void => this.setState({isOpen: false});
}

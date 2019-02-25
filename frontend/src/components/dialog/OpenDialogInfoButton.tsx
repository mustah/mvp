import * as React from 'react';
import {Children, OnClick, Styled, Titled} from '../../types/Types';
import {ButtonInfo} from '../buttons/ButtonInfo';
import {ButtonClose} from '../buttons/DialogButtons';
import {InfoButtonProps} from '../buttons/InfoButton';
import {Row} from '../layouts/row/Row';
import {Dialog} from './Dialog';

interface Props extends InfoButtonProps, Styled, Titled {
  autoScrollBodyContent: boolean;
  children: Children;
  label: string | number;
  labelStyle?: React.CSSProperties;
  onLabelClick?: OnClick;
}

interface State {
  isOpen: boolean;
}

const infoLabelStyle: React.CSSProperties = {paddingLeft: 0};

export class OpenDialogInfoButton extends React.Component<Props, State> {

  state: State = {isOpen: false};

  render() {
    const {color, iconStyle, label, labelStyle, style, title} = this.props;
    return (
      <Row style={style}>
        <ButtonInfo
          label={label}
          color={color}
          iconStyle={iconStyle}
          labelStyle={labelStyle || infoLabelStyle}
          title={title}
        />
      </Row>
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

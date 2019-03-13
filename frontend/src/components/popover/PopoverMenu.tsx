import {default as classNames} from 'classnames';
import Menu from 'material-ui/Menu';
import Popover from 'material-ui/Popover/Popover';
import * as React from 'react';
import {popoverStyle} from '../../app/themes';
import {Clickable, OnClick, RenderFunction} from '../../types/Types';
import {Row} from '../layouts/row/Row';
import './PopoverMenu.scss';
import origin = __MaterialUI.propTypes.origin;

export interface IconProps extends React.CSSProperties {
  disabled?: boolean;
}

interface Props {
  renderPopoverContent: RenderFunction<OnClick>;
  IconComponent: React.StatelessComponent<Clickable>;
  iconProps?: IconProps;
  onRequestClose?: OnClick;
  className?: string;
  popoverClassName?: string;
  anchorOrigin?: origin;
  targetOrigin?: origin;
}

interface State {
  isOpen: boolean;
  anchorElement?: React.ReactInstance;
}

export const anchorOrigin: origin = {horizontal: 'left', vertical: 'center'};
export const targetOrigin: origin = {horizontal: 'middle', vertical: 'top'};

export class PopoverMenu extends React.Component<Props, State> {

  static defaultProps: Partial<Props> = {
    anchorOrigin: {horizontal: 'right', vertical: 'bottom'},
    targetOrigin: {horizontal: 'right', vertical: 'top'},
  };

  state: State = {isOpen: false};

  render() {
    const {isOpen, anchorElement} = this.state;
    const {
      IconComponent,
      iconProps,
      className,
      popoverClassName,
      anchorOrigin,
      targetOrigin,
      renderPopoverContent
    } = this.props;

    return (
      <Row className={classNames('PopoverMenu', className)}>
        <IconComponent {...iconProps} onClick={this.onOpenMenu}/>
        <Popover
          className={classNames('PopoverMenu-Component', popoverClassName)}
          open={isOpen}
          anchorEl={anchorElement}
          anchorOrigin={anchorOrigin}
          targetOrigin={targetOrigin}
          onRequestClose={this.close}
          style={popoverStyle}
        >
          <Menu>
            {renderPopoverContent(this.close)}
          </Menu>
        </Popover>
      </Row>
    );
  }

  onOpenMenu = (event: any): void => {
    event.preventDefault();
    this.setState({
      isOpen: true,
      anchorElement: event.currentTarget,
    });
  }

  close = () => {
    const {onRequestClose} = this.props;
    this.setState({isOpen: false}, () => {
      if (onRequestClose) {
        onRequestClose();
      }
    });
  }
}

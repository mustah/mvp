import * as classNames from 'classnames';
import Menu from 'material-ui/Menu';
import Popover from 'material-ui/Popover/Popover';
import * as React from 'react';
import {Children, Clickable, OnClick} from '../../types/Types';
import {Row} from '../layouts/row/Row';
import origin = __MaterialUI.propTypes.origin;

interface Props {
  children?: Children;
  IconComponent: React.StatelessComponent<Clickable>;
  onRequestClose?: OnClick;
  className?: string;
  anchorOrigin?: origin;
  targetOrigin?: origin;
}

interface State {
  isOpen: boolean;
  anchorElement?: React.ReactInstance;
}

export class PopoverMenu extends React.Component<Props, State> {

  static defaultProps: Partial<Props> = {
    anchorOrigin: {horizontal: 'right', vertical: 'bottom'},
    targetOrigin: {horizontal: 'right', vertical: 'top'},
  };

  state: State = {isOpen: false};

  render() {
    const {isOpen, anchorElement} = this.state;
    const {IconComponent, className, anchorOrigin, targetOrigin} = this.props;

    return (
      <Row className={classNames('PopoverMenu', className)}>
        <IconComponent onClick={this.onOpenMenu}/>
        <Popover
          open={isOpen}
          anchorEl={anchorElement}
          anchorOrigin={anchorOrigin}
          targetOrigin={targetOrigin}
          onRequestClose={this.close}
        >
          <Menu>
            {this.props.children}
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

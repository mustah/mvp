import * as classNames from 'classnames';
import Menu from 'material-ui/Menu';
import Popover from 'material-ui/Popover/Popover';
import * as React from 'react';
import {wrapComponent} from '../../helpers/componentHelpers';
import {Children, Clickable, OnClick} from '../../types/Types';
import {IconMore} from '../icons/IconMore';
import {Row} from '../layouts/row/Row';
import origin = __MaterialUI.propTypes.origin;

interface Props {
  children?: Children;
  IconComponent?: React.StatelessComponent<Clickable>;
  onRequestClose?: OnClick;
  className?: string;
}

interface State {
  isOpen: boolean;
  anchorElement?: React.ReactInstance;
}

const anchorOrigin: origin = {horizontal: 'right', vertical: 'bottom'};
const targetOrigin: origin = {horizontal: 'right', vertical: 'top'};

export class PopoverMenu extends React.Component<Props, State> {

  state: State = {isOpen: false};

  render() {
    const {isOpen, anchorElement} = this.state;
    const {IconComponent, className} = this.props;

    const OpenIconComponent =
      wrapComponent<Clickable>(IconComponent || IconMore);

    return (
      <Row className={classNames('PopoverMenu', className)}>
        {OpenIconComponent({onClick: this.onOpenMenu})}
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

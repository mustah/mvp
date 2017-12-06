import * as classNames from 'classnames';
import Menu from 'material-ui/Menu';
import Popover from 'material-ui/Popover/Popover';
import * as React from 'react';
import {wrapComponent} from '../../helpers/componentHelpers';
import {Children, Clickable, NativeEvent, OnClick} from '../../types/Types';
import {IconMore} from '../icons/IconMore';
import {Row} from '../layouts/row/Row';

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

export class PopoverMenu extends React.Component<Props, State> {

  constructor(props) {
    super(props);
    this.state = {isOpen: false};
  }

  render() {
    const {isOpen, anchorElement} = this.state;
    const {IconComponent, className} = this.props;

    const OpenIconComponent = wrapComponent<Clickable>(IconComponent || IconMore);

    return (
      <Row className={classNames('PopoverMenu', className)}>
        {OpenIconComponent({onClick: this.onOpenMenu})}
        <Popover
          open={isOpen}
          anchorEl={anchorElement}
          anchorOrigin={{horizontal: 'right', vertical: 'bottom'}}
          targetOrigin={{horizontal: 'right', vertical: 'top'}}
          onRequestClose={this.close}
        >
          <Menu>
            {this.props.children}
          </Menu>
        </Popover>
      </Row>
    );
  }

  onOpenMenu = ({preventDefault, currentTarget}: NativeEvent): void => {
    preventDefault();
    this.setState({
      isOpen: true,
      anchorElement: currentTarget,
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

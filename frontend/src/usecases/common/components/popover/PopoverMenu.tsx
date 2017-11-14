import Menu from 'material-ui/Menu';
import Popover from 'material-ui/Popover/Popover';
import * as React from 'react';
import {Children} from '../../../../types/Types';
import {IconMore} from '../icons/IconMore';

interface Props {
  children?: Children;
}

interface State {
  isOpen: boolean;
  anchorElement?: React.ReactInstance;
}

export class PopoverMenu extends React.Component<Props, State> {

  constructor(props) {
    super(props);
    this.state = {
      isOpen: false,
    };
  }

  render() {
    const {isOpen, anchorElement} = this.state;

    return (
      <div className="PopoverMenu">
        <IconMore onClick={this.onClick}/>
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
      </div>
    );
  }

  onClick = (event: React.SyntheticEvent<any>): void => {
    event.preventDefault();
    this.setState({
      isOpen: true,
      anchorElement: event.currentTarget,
    });
  }

  close = () => {
    this.setState({isOpen: false});
  }
}

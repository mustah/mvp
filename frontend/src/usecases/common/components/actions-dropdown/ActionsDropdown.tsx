import * as React from 'react';
import {listItemStyle} from '../../../app/themes';
import MenuItem from 'material-ui/MenuItem';
import {Row} from '../layouts/row/Row';
import {IconMore} from '../icons/IconMore';
import Popover from 'material-ui/Popover/Popover';
import Menu from 'material-ui/Menu';

interface PopoverState {
  popOverOpen?: boolean;
  popOverAnchorElement?: React.ReactInstance;
}

interface ActionsDropdownProps {
  actions: string[];
  className: string;
}

export class ActionsDropdown extends React.Component<ActionsDropdownProps, PopoverState> {

  constructor(props) {
    super(props);

    this.state = {
      popOverOpen: false,
    };
  }

  render() {
    const {popOverOpen, popOverAnchorElement} = this.state;
    const {actions, className} = this.props;

    const onClick = (event: React.SyntheticEvent<any>): void => {
      event.preventDefault();
      this.setState({popOverOpen: true, popOverAnchorElement: event.currentTarget});
    };

    const closePopOver = () => {
      this.setState({popOverOpen: false});
    };

    const renderActions = (action, index) => (
      <MenuItem key={index} style={listItemStyle} className="first-uppercase">
        {action}
      </MenuItem>
    );
    return (
        <Row className={className}>
          <IconMore onClick={onClick}/>
          <Popover
            open={popOverOpen}
            anchorEl={popOverAnchorElement}
            anchorOrigin={{horizontal: 'left', vertical: 'bottom'}}
            targetOrigin={{horizontal: 'right', vertical: 'top'}}
            onRequestClose={closePopOver}
          >
            <Menu>
              {actions.map(renderActions)}
            </Menu>
          </Popover>
        </Row>
    );
  }
}

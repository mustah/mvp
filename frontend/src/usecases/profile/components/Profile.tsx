import Menu from 'material-ui/Menu';
import MenuItem from 'material-ui/MenuItem';
import Popover from 'material-ui/Popover/Popover';
import * as React from 'react';
import {User} from '../../auth/authReducer';
import {Column} from '../../common/components/layouts/column/Column';
import {Row} from '../../common/components/layouts/row/Row';
import {Avatar} from './Avatar';
import './Profile.scss';
import {ProfileName} from './ProfileName';

interface ProfileProps {
  user?: User;
  logout: () => any;
}

interface ProfileState {
  isOpen: boolean;
  anchorElement?: React.ReactInstance;
}

export class Profile extends React.Component<ProfileProps, ProfileState> {

  constructor(props) {
    super(props);
    this.state = {
      isOpen: false,
    };
  }

  render() {
    const {user} = this.props;
    const {isOpen} = this.state;
    return (
      <Column className="flex-1">
        <Row className="Profile">
          {user && <ProfileName user={user}/>}
          <Avatar user={user} onClick={this.openMenu}/>
          <Popover
            open={isOpen}
            anchorEl={this.state.anchorElement}
            anchorOrigin={{horizontal: 'right', vertical: 'bottom'}}
            targetOrigin={{horizontal: 'right', vertical: 'top'}}
            onRequestClose={this.closeMenu}
          >
            <Menu>
              {user && <MenuItem className="logout" onClick={this.logout}>Logout</MenuItem>}
            </Menu>
          </Popover>
        </Row>
      </Column>
    );
  }

  openMenu = (event: React.SyntheticEvent<any>): void => {
    event.preventDefault();
    this.setState({isOpen: true, anchorElement: event.currentTarget});
  }

  logout = (): void => {
    this.closeMenu();
    this.props.logout();
  }

  closeMenu = (): void => {
    this.setState({isOpen: false});
  }

}

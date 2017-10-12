import IconButton from 'material-ui/IconButton';
import Menu from 'material-ui/Menu';
import MenuItem from 'material-ui/MenuItem';
import Popover from 'material-ui/Popover/Popover';
import * as React from 'react';
import {User} from '../../auth/authReducer';
import {Icon} from '../../common/components/icons/Icons';
import {Column} from '../../common/components/layouts/column/Column';
import {Row} from '../../common/components/layouts/row/Row';
import {MenuSeparator} from '../../topmenu/components/separators/MenuSeparator';
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

const avatarStyle = {
  padding: '0 0 0 10px',
  height: '24px',
  width: '34px',
};

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
          <IconButton
            disabled={!user}
            style={avatarStyle}
            onClick={this.openMenu}
          >
            <Icon name="account-circle"/>
          </IconButton>
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
        <MenuSeparator/>
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

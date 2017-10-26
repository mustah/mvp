import Menu from 'material-ui/Menu';
import MenuItem from 'material-ui/MenuItem';
import Popover from 'material-ui/Popover/Popover';
import * as React from 'react';
import {translate} from '../../../services/translationService';
import {User} from '../../auth/authReducer';
import {Column} from '../../common/components/layouts/column/Column';
import {Row} from '../../common/components/layouts/row/Row';
import {Xsmall} from '../../common/components/texts/Texts';
import {Avatar} from './IconAvatar';
import './Profile.scss';

interface ProfileProps {
  user: User;
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
    const {isOpen, anchorElement} = this.state;
    return (
      <Column className="ProfileWrapper">
        <Row className="Profile">
          <Avatar onClick={this.openMenu}/>
          <Popover
            open={isOpen}
            anchorEl={anchorElement}
            anchorOrigin={{horizontal: 'right', vertical: 'bottom'}}
            targetOrigin={{horizontal: 'right', vertical: 'top'}}
            onRequestClose={this.closeMenu}
          >
            <Menu>
              <MenuItem className="logout first-uppercase" onClick={this.logout}>
                {translate('logout')}
              </MenuItem>
            </Menu>
          </Popover>
        </Row>
        <Row className="Row-center">
          <Xsmall className="Bold">{user.firstName}</Xsmall>
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

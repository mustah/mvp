import MenuItem from 'material-ui/MenuItem';
import * as React from 'react';
import {translate} from '../../../services/translationService';
import {OnClick} from '../../../types/Types';
import {menuItemInnerDivStyle} from '../../app/themes';
import {User} from '../../auth/authReducer';
import {Column} from '../../common/components/layouts/column/Column';
import {Row} from '../../common/components/layouts/row/Row';
import {PopoverMenu} from '../../common/components/popover/PopoverMenu';
import {Xsmall} from '../../common/components/texts/Texts';
import {Avatar} from './IconAvatar';
import './Profile.scss';

interface Props {
  user: User;
  logout: OnClick;
}

export const Profile = (props: Props) => {
  const {user, logout} = props;

  return (
    <Column className="ProfileWrapper">
      <Row className="Profile">
        <PopoverMenu IconComponent={Avatar}>
          <MenuItem
            style={menuItemInnerDivStyle}
            className="first-uppercase"
            onClick={logout}
          >
            {translate('logout')}
          </MenuItem>
        </PopoverMenu>
      </Row>
      <Row className="Row-center">
        <Xsmall className="Bold">{user.firstName}</Xsmall>
      </Row>
    </Column>
  );
};

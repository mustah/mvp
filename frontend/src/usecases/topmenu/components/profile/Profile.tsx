import MenuItem from 'material-ui/MenuItem';
import * as React from 'react';
import {menuItemInnerDivStyle} from '../../../../app/themes';
import {IconAvatar} from '../../../../components/icons/IconAvatar';
import {Column} from '../../../../components/layouts/column/Column';
import {Row} from '../../../../components/layouts/row/Row';
import {PopoverMenu} from '../../../../components/popover/PopoverMenu';
import {Xsmall} from '../../../../components/texts/Texts';
import {translate} from '../../../../services/translationService';
import {User} from '../../../../state/domain-models/user/userModels';
import {uuid} from '../../../../types/Types';
import './Profile.scss';

interface Props {
  user: User;
  logout: (organisationId: uuid) => void;
}

export const Profile = (props: Props) => {
  const {user, logout} = props;
  const logoutClick = () => logout(user.organisation.code);
  return (
    <Column className="ProfileWrapper">
      <Row className="Profile">
        <PopoverMenu IconComponent={IconAvatar}>
          <MenuItem
            style={menuItemInnerDivStyle}
            className="first-uppercase"
            onClick={logoutClick}
          >
            {translate('logout')}
          </MenuItem>
        </PopoverMenu>
      </Row>
      <Row className="Row-center">
        <Xsmall className="Bold">{user.name}</Xsmall>
      </Row>
    </Column>
  );
};

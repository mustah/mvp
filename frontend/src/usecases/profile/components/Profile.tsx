import * as React from 'react';
import {User} from '../../auth/authReducer';
import {Icon} from '../../common/components/icons/Icons';
import {Column} from '../../common/components/layouts/column/Column';
import {Row} from '../../common/components/layouts/row/Row';
import {Normal} from '../../common/components/texts/Texts';
import {MenuSeparator} from '../../topmenu/components/separators/MenuSeparator';
import './Profile.scss';
import {ProfileName} from './ProfileName';

export interface ProfileProps {
  user?: User;
  logout: () => any;
}

export const Profile = (props: ProfileProps) => {
  const {user, logout} = props;
  return (
    <Column className="flex-1">
      <Row className="Profile">
        {user && <ProfileName user={user}/>}
        <Icon name="account-circle"/>
        {user && <Normal className="logout" onClick={logout}>Logout</Normal>}
      </Row>
      <MenuSeparator/>
    </Column>
  );
};

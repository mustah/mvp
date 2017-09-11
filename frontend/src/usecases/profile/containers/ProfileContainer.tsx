import * as React from 'react';
import {User} from '../../auth/authReducer';
import {Icon} from '../../common/components/icons/Icons';
import {Normal} from '../../common/components/texts/Texts';
import {Column} from '../../layouts/components/column/Column';
import {Row} from '../../layouts/components/row/Row';
import {MenuSeparator} from '../../topmenu/components/separators/MenuSeparator';
import {ProfileName} from '../components/ProfileName';
import './ProfileContainer.scss';

export interface ProfileContainerProps {
  user?: User;
  logout: () => any;
}

export const ProfileContainer = (props: ProfileContainerProps) => {
  const {user, logout} = props;
  return (
    <Column className="flex-1">
      <Row className="ProfileContainer">
        {user && <ProfileName user={user}/>}
        <Icon name="account-circle"/>
        {user && <Normal className="logout" onClick={logout}>Logout</Normal>}
      </Row>
      <MenuSeparator/>
    </Column>
  );
};

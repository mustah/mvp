import * as React from 'react';
import {User} from '../../auth/authReducer';
import {Icon} from '../../common/components/icons/Icons';
import {Column} from '../../layouts/components/column/Column';
import {Row} from '../../layouts/components/row/Row';
import {MenuSeparator} from '../../topmenu/components/separators/MenuSeparator';
import {ProfileName} from '../components/ProfileName';
import './ProfileContainer.scss';

export interface ProfileContainerProps {
  user?: User;
}

export const ProfileContainer = (props: ProfileContainerProps) => {
  const {user} = props;
  return (
    <Column className="flex-1">
      <Row className="ProfileContainer">
        {user && <ProfileName user={user}/>}
        <Icon name="account-circle"/>
      </Row>
      <MenuSeparator/>
    </Column>
  );
};

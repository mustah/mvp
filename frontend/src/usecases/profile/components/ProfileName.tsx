import * as React from 'react';
import {User} from '../../auth/authReducer';
import {Bold, Small} from '../../common/components/texts/Texts';
import {Column} from '../../layouts/components/column/Column';

export interface ProfileNameProps {
  user: User;
}

export const ProfileName = (props: ProfileNameProps) => (
  <Column>
    <Bold>{props.user.firstName} {props.user.lastName}</Bold>
    <Small className="text-align-right">{props.user.company}</Small>
  </Column>
);

import IconButton from 'material-ui/IconButton';
import * as React from 'react';
import {User} from '../../auth/authReducer';
import {Icon} from '../../common/components/icons/Icons';

const avatarStyle = {
  padding: '0 0 0 10px',
  height: '24px',
  width: '34px',
};

interface AvatarProps {
  user?: User;
  onClick?: (...args) => void;
}

export const Avatar = (props: AvatarProps) => {
  const {user, onClick} = props;
  return (
    <IconButton
      disabled={!user}
      style={avatarStyle}
      onClick={onClick}
    >
      <Icon name="account-circle"/>
    </IconButton>
  );
};

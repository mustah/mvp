import IconButton from 'material-ui/IconButton';
import ActionAccountCircle from 'material-ui/svg-icons/action/account-circle';
import * as React from 'react';
import {User} from '../../auth/authReducer';

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
      <ActionAccountCircle/>
    </IconButton>
  );
};

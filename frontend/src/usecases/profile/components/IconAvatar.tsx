import IconButton from 'material-ui/IconButton';
import ActionAccountCircle from 'material-ui/svg-icons/action/account-circle';
import * as React from 'react';

const avatarStyle = {
  padding: 0,
  height: 24,
  width: 34,
};

interface AvatarProps {
  onClick?: (...args) => void;
}

export const Avatar = (props: AvatarProps) => {
  const {onClick} = props;
  return (
    <IconButton style={avatarStyle} onClick={onClick}>
      <ActionAccountCircle color="white"/>
    </IconButton>
  );
};

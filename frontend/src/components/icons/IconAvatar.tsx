import IconButton from 'material-ui/IconButton';
import ActionAccountCircle from 'material-ui/svg-icons/action/account-circle';
import * as React from 'react';
import {colors} from '../../app/themes';

const avatarStyle: React.CSSProperties = {
  padding: 0,
  height: 36,
  width: 36,
};

export const IconAvatar = () => (
  <IconButton style={avatarStyle} iconStyle={avatarStyle}>
    <ActionAccountCircle color={colors.white} style={avatarStyle}/>
  </IconButton>
);

import IconButton from 'material-ui/IconButton';
import ActionAccountCircle from 'material-ui/svg-icons/action/account-circle';
import * as React from 'react';
import {Clickable} from '../../../types/Types';
import {colors} from '../../app/themes';

const avatarStyle = {
  padding: 0,
  height: 24,
  width: 34,
};

export const Avatar = (props: Clickable) => {
  const {onClick} = props;
  return (
    <IconButton style={avatarStyle} onClick={onClick}>
      <ActionAccountCircle color={colors.darkBlue}/>
    </IconButton>
  );
};

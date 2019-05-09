import FlatButton from 'material-ui/FlatButton';
import ActionDelete from 'material-ui/svg-icons/action/delete';
import * as React from 'react';
import {colors} from '../../app/colors';
import {Clickable} from '../../types/Types';

export const ButtonDelete = ({onClick}: Clickable) => (
  <FlatButton
    hoverColor="inherit"
    icon={<ActionDelete color={colors.primaryBg}/>}
    onClick={onClick}
    style={{minWidth: 24}}
  />
);

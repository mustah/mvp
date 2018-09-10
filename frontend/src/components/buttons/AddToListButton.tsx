import IconButton from 'material-ui/IconButton';
import AvPlaylistAdd from 'material-ui/svg-icons/av/playlist-add';
import * as React from 'react';
import {colors, iconStyle} from '../../app/themes';
import {Clickable} from '../../types/Types';

export const AddToListButton = ({onClick}: Clickable) => (
  <IconButton onClick={onClick} className="AddToListButton">
    <AvPlaylistAdd style={iconStyle} color={colors.lightBlack} hoverColor={colors.iconHover}/>/>
  </IconButton>
);

import IconButton from 'material-ui/IconButton';
import ActionZoomIn from 'material-ui/svg-icons/action/zoom-in';
import * as React from 'react';
import {colors, iconStyle} from '../../app/themes';
import {Clickable} from '../../types/Types';

export const ZoomButton = ({onClick}: Clickable) => (
  <IconButton onClick={onClick} className="ZoomButton">
    <ActionZoomIn style={iconStyle} color={colors.lightBlack} hoverColor={colors.iconHover}/>/>
  </IconButton>
);

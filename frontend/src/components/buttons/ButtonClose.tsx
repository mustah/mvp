import IconButton from 'material-ui/IconButton';
import NavigationClose from 'material-ui/svg-icons/navigation/close';
import * as React from 'react';
import {colors, iconSizeMedium, iconStyle} from '../../app/themes';
import {Clickable, Styled} from '../../types/Types';

type Props = Clickable & Styled;

export const ButtonClose = ({onClick, style}: Props) => (
  <IconButton onClick={onClick} style={{...iconSizeMedium, ...style}}>
    <NavigationClose style={iconStyle} color={colors.lightBlack} hoverColor={colors.iconHover}/>
  </IconButton>
);

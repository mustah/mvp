import IconButton from 'material-ui/IconButton';
import NavigationClose from 'material-ui/svg-icons/navigation/close';
import * as React from 'react';
import {iconSizeMedium, iconStyle} from '../../app/themes';
import {Clickable, Styled} from '../../types/Types';
import {ThemeContext, withCssStyles} from '../hoc/withThemeProvider';

type Props = Clickable & Styled & ThemeContext;

export const ButtonClose = withCssStyles(({cssStyles: {primary}, onClick, style}: Props) => (
  <IconButton onClick={onClick} style={{...iconSizeMedium, ...style}}>
    <NavigationClose style={iconStyle} color={primary.fg} hoverColor={primary.fgHover}/>
  </IconButton>
));

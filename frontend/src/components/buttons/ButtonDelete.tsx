import ActionDelete from 'material-ui/svg-icons/action/delete';
import * as React from 'react';
import {Clickable} from '../../types/Types';
import {ThemeContext, withCssStyles} from '../hoc/withThemeProvider';
import {ButtonPrimary} from './ButtonPrimary';

export const ButtonDelete = withCssStyles(({onClick, cssStyles: {primary}}: Clickable & ThemeContext) => (
  <ButtonPrimary
    hoverColor="inherit"
    icon={<ActionDelete color={primary.bg}/>}
    onClick={onClick}
    style={{minWidth: 24}}
  />
));

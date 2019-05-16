import FlatButton from 'material-ui/FlatButton';
import * as React from 'react';
import {ThemeContext, withCssStyles} from '../hoc/withThemeProvider';
import FlatButtonProps = __MaterialUI.FlatButtonProps;

type Props = FlatButtonProps & ThemeContext;

export const ButtonSecondary = withCssStyles(({cssStyles: {secondary}, ...props}: Props) => (
  <FlatButton {...props} style={{backgroundColor: secondary.bgActive, color: secondary.fgActive, ...props.style}}/>
));

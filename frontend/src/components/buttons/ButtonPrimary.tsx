import FlatButton from 'material-ui/FlatButton';
import * as React from 'react';
import {ThemeContext, withCssStyles} from '../hoc/withThemeProvider';
import FlatButtonProps = __MaterialUI.FlatButtonProps;

type Props = FlatButtonProps & ThemeContext;

export const ButtonPrimary = withCssStyles(({cssStyles: {primary}, ...props}: Props) => (
  <FlatButton{...props} style={{backgroundColor: primary.bg, ...props.style}}/>
));

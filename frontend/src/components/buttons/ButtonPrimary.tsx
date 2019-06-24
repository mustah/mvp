import FlatButton from 'material-ui/FlatButton';
import * as React from 'react';
import {colors} from '../../app/colors';
import {ThemeContext, withCssStyles} from '../hoc/withThemeProvider';
import FlatButtonProps = __MaterialUI.FlatButtonProps;

type Props = FlatButtonProps & ThemeContext;

export const ButtonPrimary = withCssStyles(({cssStyles: {primary}, ...props}: Props) => (
  <FlatButton
    {...props}
    style={{backgroundColor: props.disabled ? colors.disabledColor : primary.bg, ...props.style}}
  />
));

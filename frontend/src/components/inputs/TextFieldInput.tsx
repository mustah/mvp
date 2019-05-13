import {default as classNames} from 'classnames';
import {TextFieldProps} from 'material-ui';
import TextField from 'material-ui/TextField';
import * as React from 'react';
import {underlineFocusStyle} from '../../app/themes';
import {ThemeContext, withCssStyles} from '../hoc/withThemeProvider';

export const TextFieldInput =
  withCssStyles(({cssStyles: {primary}, className, ...props}: TextFieldProps & ThemeContext) => (
    <TextField
      className={classNames('TextField', className)}
      floatingLabelFocusStyle={{color: primary.bg}}
      underlineFocusStyle={{...underlineFocusStyle, borderColor: primary.bg}}
      {...props}
    />
  ));

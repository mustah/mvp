import {default as classNames} from 'classnames';
import {TextFieldProps} from 'material-ui';
import TextField from 'material-ui/TextField';
import * as React from 'react';
import {floatingLabelFocusStyle, underlineFocusStyle} from '../../app/themes';

export const TextFieldInput = ({className, ...props}: TextFieldProps) => (
  <TextField
    className={classNames('TextField', className)}
    floatingLabelFocusStyle={floatingLabelFocusStyle}
    underlineFocusStyle={underlineFocusStyle}
    {...props}
  />
);

import TextField from 'material-ui/TextField';
import * as React from 'react';
import {floatingLabelFocusStyle, underlineFocusStyle} from '../../app/themes';
import {ClassNamed} from '../../types/Types';
import classNames = require('classnames');

interface TextFieldInputProps extends ClassNamed {
  id: string;
  floatingLabelText: string;
  hintText: string;
  value: string;
  onChange: (...arg) => void;
  type?: 'password';
  disabled?: boolean;
}

export const TextFieldInput = ({className, ...props}: TextFieldInputProps) => (
  <TextField
    className={classNames('TextField', className)}
    floatingLabelFocusStyle={floatingLabelFocusStyle}
    underlineFocusStyle={underlineFocusStyle}
    {...props}
  />
);

import {default as classNames} from 'classnames';
import {TextFieldProps} from 'material-ui';
import * as React from 'react';
import {TextValidator} from 'react-material-ui-form-validator';
import {floatingLabelFocusStyle, underlineFocusStyle} from '../../app/themes';

export interface ValidatorProps {
  validators: string[];
  errorMessages: string[];
}

type Props = TextFieldProps & ValidatorProps;

export const ValidatedFieldInput = ({className, id, ...props}: Props) => (
  <TextValidator
    className={classNames('TextField', className)}
    floatingLabelFocusStyle={floatingLabelFocusStyle}
    underlineFocusStyle={underlineFocusStyle}
    id={id}
    name={id}
    {...props}
  />
);

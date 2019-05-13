import {default as classNames} from 'classnames';
import {TextFieldProps} from 'material-ui';
import * as React from 'react';
import {TextValidator} from 'react-material-ui-form-validator';
import {underlineFocusStyle} from '../../app/themes';
import {ThemeContext, withCssStyles} from '../hoc/withThemeProvider';

export interface ValidatorProps {
  validators: string[];
  errorMessages: string[];
}

type Props = TextFieldProps & ValidatorProps & ThemeContext;

export const ValidatedFieldInput = withCssStyles(({cssStyles: {primary}, className, id, ...props}: Props) => (
  <TextValidator
    className={classNames('TextField', className)}
    floatingLabelFocusStyle={{color: primary.bg}}
    underlineFocusStyle={{...underlineFocusStyle, borderColor: primary.bg}}
    id={id}
    name={id}
    {...props}
  />
));

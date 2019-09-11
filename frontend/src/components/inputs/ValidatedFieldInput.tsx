import {default as classNames} from 'classnames';
import {TextFieldProps} from 'material-ui';
import * as React from 'react';
import {TextValidator} from 'react-material-ui-form-validator';
import {underlineFocusStyle} from '../../app/themes';
import {firstUpperTranslated} from '../../services/translationService';
import {ThemeContext, withCssStyles} from '../hoc/withThemeProvider';

export interface ValidatorProps {
  validators?: string[];
  errorMessages?: string[];
  labelText?: string;
}

type Props = TextFieldProps & ValidatorProps & ThemeContext;

export const ValidatedFieldInput =
  withCssStyles(({cssStyles: {primary}, className, id, labelText, ...props}: Props) => {
    const errorMessages: string[] = [firstUpperTranslated('required field')];
    const validators: string[] = ['required'];

    return (
      <TextValidator
        className={classNames('TextField', className)}
        errorMessages={errorMessages}
        validators={validators}
        floatingLabelFocusStyle={{color: primary.bg}}
        underlineFocusStyle={{...underlineFocusStyle, borderColor: primary.bg}}
        floatingLabelText={labelText}
        hintText={labelText}
        id={id}
        name={id}
        {...props}
      />
    );
  });

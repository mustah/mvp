import {default as classNames} from 'classnames';
import * as React from 'react';
import {SelectValidator} from 'react-material-ui-form-validator';
import {underlineFocusStyle} from '../../app/themes';
import {firstUpperTranslated} from '../../services/translationService';
import {ThemeContext, withCssStyles} from '../hoc/withThemeProvider';
import {MultipleOrSingle, renderMenuItem, SelectFieldInputProps, WrappedSelectFieldProps} from './InputSelectable';
import {ValidatorProps} from './ValidatedFieldInput';

type Props = SelectFieldInputProps & MultipleOrSingle & ValidatorProps & ThemeContext;

const WrappedSelectValidatorField = (props: WrappedSelectFieldProps) => <SelectValidator {...props} />;

export const ValidatedInputSelectable =
  withCssStyles(({cssStyles: {primary}, className, id, labelText, options, ...props}: Props) => {
    const renderedItems = options.map(renderMenuItem);
    const errorMessages: string[] = [firstUpperTranslated('required field')];
    const validators: string[] = ['required'];

    return (
      <WrappedSelectValidatorField
        className={classNames('SelectField', className)}
        errorMessages={errorMessages}
        validators={validators}
        floatingLabelFocusStyle={{color: primary.bg}}
        floatingLabelText={labelText}
        hintText={labelText}
        underlineFocusStyle={{...underlineFocusStyle, borderColor: primary.bg}}
        name={id}
        {...props}
      >
        {renderedItems}
      </WrappedSelectValidatorField>
    );
  });

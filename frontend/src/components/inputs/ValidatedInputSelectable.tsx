import {default as classNames} from 'classnames';
import * as React from 'react';
import {SelectValidator} from 'react-material-ui-form-validator';
import {underlineFocusStyle} from '../../app/themes';
import {ThemeContext, withCssStyles} from '../hoc/withThemeProvider';
import {MultipleOrSingle, renderMenuItem, SelectFieldInputProps, WrappedSelectFieldProps} from './InputSelectable';
import {ValidatorProps} from './ValidatedFieldInput';

type Props = SelectFieldInputProps & MultipleOrSingle & ValidatorProps & ThemeContext;

const WrappedSelectValidatorField = (props: WrappedSelectFieldProps) => <SelectValidator {...props} />;

export const ValidatedInputSelectable =
  withCssStyles(({cssStyles: {primary}, className, id, options, ...props}: Props) => {
    const renderedItems = options.map(renderMenuItem);
    return (
      <WrappedSelectValidatorField
        className={classNames('SelectField', className)}
        floatingLabelFocusStyle={{color: primary.bg}}
        underlineFocusStyle={{...underlineFocusStyle, borderColor: primary.bg}}
        name={id}
        {...props}
      >
        {renderedItems}
      </WrappedSelectValidatorField>
    );
  });

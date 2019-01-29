import {default as classNames} from 'classnames';
import * as React from 'react';
import {SelectValidator} from 'react-material-ui-form-validator';
import {floatingLabelFocusStyle, underlineFocusStyle} from '../../app/themes';
import {MultipleOrSingle, renderMenuItem, SelectFieldInputProps, WrappedSelectFieldProps} from './InputSelectable';
import {ValidatorProps} from './ValidatedFieldInput';

type Props = SelectFieldInputProps & MultipleOrSingle & ValidatorProps;

const WrappedSelectValidatorField = (props: WrappedSelectFieldProps) => <SelectValidator {...props} />;

export const ValidatedInputSelectable = ({className, id, options, ...props}: Props) => {
  const renderedItems = options.map(renderMenuItem);

  return (
    <WrappedSelectValidatorField
      className={classNames('SelectField', className)}
      floatingLabelFocusStyle={floatingLabelFocusStyle}
      underlineFocusStyle={underlineFocusStyle}
      name={id}
      {...props}
    >
      {renderedItems}
    </WrappedSelectValidatorField>
  );
};

import MenuItem from 'material-ui/MenuItem';
import SelectField from 'material-ui/SelectField';
import * as React from 'react';
import {floatingLabelFocusStyle, underlineFocusStyle} from '../../app/themes';
import {ClassNamed, IdNamed, ItemOrArray, uuid} from '../../types/Types';
import classNames = require('classnames');
import SelectFieldProps = __MaterialUI.SelectFieldProps;

interface SelectFieldInputProps extends ClassNamed {
  id: string;
  options: IdNamed[];
  value: ItemOrArray<uuid>;
  floatingLabelText: string;
  hintText: string;
  multiple?: boolean;
  onChange: (...args) => void;
  disabled: boolean;
}

export const SelectFieldInput = ({className, options, ...props}: SelectFieldInputProps) => {
  const renderMenuItems = ({id, name}: IdNamed, index) =>
    <MenuItem key={index} value={id} primaryText={name}/>;
  return (
    <WrappedSelectField
      className={classNames('SelectField', className)}
      floatingLabelFocusStyle={floatingLabelFocusStyle}
      underlineFocusStyle={underlineFocusStyle}
      {...props}
    >
      {options.map(renderMenuItems)}
    </WrappedSelectField>
  );
};

/*
TODO: WrappedSelectField is used as a workaround until @types/material-ui/SelectField is
supporting floatingLabelFocusStyle.
*/
interface WrappedSelectFieldProps extends SelectFieldProps {
  floatingLabelFocusStyle?: React.CSSProperties;
}

const WrappedSelectField = (props: WrappedSelectFieldProps) => <SelectField {...props} />;

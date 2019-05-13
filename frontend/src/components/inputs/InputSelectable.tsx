import {default as classNames} from 'classnames';
import MenuItem from 'material-ui/MenuItem';
import SelectField from 'material-ui/SelectField';
import * as React from 'react';
import {underlineFocusStyle} from '../../app/themes';
import {ClassNamed, IdNamed, OnChange, uuid} from '../../types/Types';
import {ThemeContext, withCssStyles} from '../hoc/withThemeProvider';
import SelectFieldProps = __MaterialUI.SelectFieldProps;

export const renderMenuItem = ({id, name}: IdNamed) =>
  <MenuItem key={id} value={id} primaryText={name}/>;

export type MultipleOrSingle = {multiple: false; value: uuid} | {multiple: true; value: uuid[]};

export interface SelectFieldInputProps extends ClassNamed {
  id: string;
  options: IdNamed[];
  floatingLabelText: string;
  hintText: string;
  onChange: OnChange;
  disabled?: boolean;
}

type Props = SelectFieldInputProps & MultipleOrSingle & ThemeContext;

/**
 * WrappedSelectField is used as a workaround until @types/material-ui/SelectField is
 * supporting floatingLabelFocusStyle.
 */
export interface WrappedSelectFieldProps extends SelectFieldProps {
  floatingLabelFocusStyle?: React.CSSProperties;
}

const WrappedSelectField = (props: WrappedSelectFieldProps) => <SelectField {...props} />;

export const SelectFieldInput = withCssStyles(({cssStyles: {primary}, className, options, ...props}: Props) => {
  const renderedItems = options.map(renderMenuItem);
  return (
    <WrappedSelectField
      className={classNames('SelectField', className)}
      floatingLabelFocusStyle={{color: primary.bg}}
      underlineFocusStyle={{...underlineFocusStyle, borderColor: primary.bg}}
      {...props}
    >
      {renderedItems}
    </WrappedSelectField>
  );
});

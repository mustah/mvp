import React from 'react';
import {Overwrite} from 'utility-types';
import {IdNamed} from '../../types/Types';
import {SelectFieldInput, SelectFieldInputProps} from './InputSelectable';
import Maybe = jest.Maybe;

export interface CellProps {
  inEdit: boolean;
  dataItem: any;
  value: Maybe<IdNamed>;
}

type Props = Overwrite<SelectFieldInputProps, {floatingLabelText?: any, hintText?: any}>
  & CellProps;

export const InputSelectableCell = ({onChange, dataItem, value, inEdit, ...props}: Props) => {
  const handleChange = (e, _, value) => onChange({dataItem, field: dataItem.field, syntheticEvent: e, value});

  if (!inEdit) {
    return (
      <td>
        {value ? value.name : ''}
      </td>
    );
  }

  return (
    <td>
      <SelectFieldInput
        {...props}
        floatingLabelText=""
        hintText=""
        multiple={false}
        onChange={handleChange}
        value={value ? value.id : ''}
      />
    </td>
  );
};

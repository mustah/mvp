import 'Checkbox.scss';
import * as React from 'react';
import {Clickable} from '../../../types/Types';
import {RowMiddle} from '../../common/components/layouts/row/Row';
import {Normal} from '../../common/components/texts/Texts';

export interface CheckboxProps {
  isChecked?: boolean;
  label: string;
  data?: any;
}

interface CheckboxListProps extends Clickable {
  list: CheckboxProps[];
}

export const Checkbox = (props: CheckboxProps & Clickable) => {
  const onClick = () => props.onClick(
    {
      value: props.label,
      isChecked: !props.isChecked,

    });
  return (
    <RowMiddle className="Checkbox">
      <input
        type="checkbox"
        checked={props.isChecked}
        id={props.label}
        onClick={onClick}
      />
      <label htmlFor={props.label} className="clickable">
        <Normal>{props.label}</Normal>
      </label>
    </RowMiddle>
  );
};

export const CheckboxList = (props: CheckboxListProps) => {
  const renderCheckbox = (checkbox: CheckboxProps, index: number) => (
    <Checkbox
      key={index}
      {...checkbox}
      onClick={props.onClick}
    />);
  return (
    <div className="CheckboxList">
      {props.list.map(renderCheckbox)}
    </div>
  );
};

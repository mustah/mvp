import './Checkbox.scss';
import * as React from 'react';
import {Clickable, IdNamed} from '../../../../types/Types';
import {RowMiddle} from '../layouts/row/Row';
import {Normal} from '../texts/Texts';

interface CheckboxListProps extends Clickable {
  list: IdNamed[];
  allChecked?: boolean;
}

type CheckBox = IdNamed & Clickable & {checked?: boolean};

export const Checkbox = (props: CheckBox) => {
  const {id, name, checked} = props;
  const onClick = () => props.onClick({name, id});
  const htmlId = `id-${id}`;
  return (
    <RowMiddle className="Checkbox">
      <input
        type="checkbox"
        id={htmlId}
        onClick={onClick}
        defaultChecked={checked}
      />
      <label htmlFor={htmlId} className="clickable">
        <Normal>{name}</Normal>
      </label>
    </RowMiddle>
  );
};

export const CheckboxList = (props: CheckboxListProps) => {
  const renderCheckbox = (checkbox: IdNamed) => (
    <Checkbox
      key={checkbox.id}
      {...checkbox}
      onClick={props.onClick}
      checked={props.allChecked}
    />);

  return (
    <div className="CheckboxList">
      {props.list.map(renderCheckbox)}
    </div>
  );
};

import 'Checkbox.scss';
import * as React from 'react';
import {Clickable, IdNamed} from '../../../types/Types';
import {RowMiddle} from '../../common/components/layouts/row/Row';
import {Normal} from '../../common/components/texts/Texts';

interface CheckboxListProps extends Clickable {
  list: IdNamed[];
}

export const Checkbox = (props: IdNamed & Clickable) => {
  const {id, name} = props;
  const onClick = () => props.onClick({name, id});
  const htmlId = `id-${id}`;

  return (
    <RowMiddle className="Checkbox">
      <input
        type="checkbox"
        id={htmlId}
        onClick={onClick}
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
    />);

  return (
    <div className="CheckboxList">
      {props.list.map(renderCheckbox)}
    </div>
  );
};

import * as React from 'react';
import {Clickable, IdNamed} from '../../../../types/Types';
import {RowMiddle} from '../layouts/row/Row';
import {Normal} from '../texts/Texts';
import './Checkbox.scss';

type CheckBox = IdNamed & Clickable & {checked?: boolean} & {key: any, style: any};

export const Checkbox = (props: CheckBox) => {
  const {id, name, checked, style, key} = props;
  const onClick = () => props.onClick({name, id});
  const htmlId = `id-${id}`;
  return (
    <RowMiddle className="Checkbox" style={style} key={key}>
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

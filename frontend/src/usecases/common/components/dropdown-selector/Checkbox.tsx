import * as React from 'react';
import {Clickable, uuid} from '../../../../types/Types';
import {RowMiddle} from '../layouts/row/Row';
import './Checkbox.scss';

interface CheckBox extends Clickable {
  id: uuid;
  checked?: boolean;
  style: any;
  label: Array<React.ReactElement<any>>;
}

export const Checkbox = (props: CheckBox) => {
  const {id, label, checked, style, onClick} = props;
  const htmlId = `id-${id}`;
  return (
    <RowMiddle className="Checkbox" style={style}>
      <input
        type="checkbox"
        id={htmlId}
        onClick={onClick}
        defaultChecked={checked}
      />
      <label htmlFor={htmlId} className="clickable">
        {label}
      </label>
    </RowMiddle>
  );
};

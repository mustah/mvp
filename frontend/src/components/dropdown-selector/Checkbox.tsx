import * as classNames from 'classnames';
import * as React from 'react';
import {Children, ClassNamed, Clickable, uuid} from '../../types/Types';
import {RowMiddle} from '../layouts/row/Row';
import './Checkbox.scss';

interface CheckBox extends Clickable, ClassNamed {
  id: uuid;
  checked?: boolean;
  style: React.CSSProperties;
  label: Children;
}

export const Checkbox = (props: CheckBox) => {
  const {id, label, checked, style, onClick, className} = props;
  const htmlId = `id-${id}`;
  return (
    <RowMiddle className="Checkbox" style={style}>
      <input
        type="checkbox"
        id={htmlId}
        onClick={onClick}
        defaultChecked={checked}
      />
      <label htmlFor={htmlId} className={classNames('clickable', className)}>
        {label}
      </label>
    </RowMiddle>
  );
};

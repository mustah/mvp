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
  const {label, checked, style, onClick, className} = props;
  return (
    <RowMiddle className="Checkbox" style={style}>
      <label className={classNames('clickable', className)}>
        <input
          type="checkbox"
          onClick={onClick}
          defaultChecked={checked}
        />
        {label}
      </label>
    </RowMiddle>
  );
};

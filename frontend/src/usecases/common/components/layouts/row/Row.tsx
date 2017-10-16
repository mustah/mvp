import * as classNames from 'classnames';
import * as React from 'react';
import './Row.scss';
import {LayoutProps} from '../layout/Layout';

export const Row = (props: LayoutProps) => {
  const {className, onClick} = props;
  return (
    <div className={classNames('Row', className)} onClick={onClick}>
      {props.children}
    </div>
  );
};

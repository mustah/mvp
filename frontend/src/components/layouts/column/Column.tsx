import * as classNames from 'classnames';
import * as React from 'react';
import {LayoutProps} from '../layout/Layout';
import './Column.scss';

export const Column = ({className, onClick, children}: LayoutProps) => {
  return (
    <div className={classNames('Column', className)} onClick={onClick}>
      {children}
    </div>
  );
};

export const ColumnCenter = (props: LayoutProps) =>
  <Column {...props} className={classNames(props.className, 'Column-center')}/>;

export const ColumnBottom = (props: LayoutProps) =>
  <Column {...props} className={classNames(props.className, 'Column-bottom')}/>;

import * as classNames from 'classnames';
import * as React from 'react';
import {LayoutProps} from '../layout/Layout';
import './Row.scss';

export const Row = (props: LayoutProps) => {
  const {className, onClick, style} = props;
  return (
    <div className={classNames('Row', className)} onClick={onClick} style={style}>
      {props.children}
    </div>
  );
};

export const RowCenter = (props: LayoutProps) =>
  <Row {...props} className={classNames(props.className, 'Row-center')}/>;

export const RowMiddle = (props: LayoutProps) =>
  <Row {...props} className={classNames(props.className, 'Row-middle')}/>;

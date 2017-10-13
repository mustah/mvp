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

export const RowCenter = (props) => <Row {...props} className={classNames(props.className, 'Row-center')}/>;

export const RowMiddle = (props) => <Row {...props} className={classNames(props.className, 'Row-middle')}/>;

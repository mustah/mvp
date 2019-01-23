import {default as classNames} from 'classnames';
import * as React from 'react';
import {LayoutProps} from '../layout/Layout';
import './Row.scss';

export const Row = ({children, className, onClick, style}: LayoutProps) => (
  <div className={classNames('Row', className)} onClick={onClick} style={style}>
    {children}
  </div>
);

export const RowCenter = (props: LayoutProps) =>
  <Row {...props} className={classNames(props.className, 'Row-center')}/>;

export const RowRight = (props: LayoutProps) =>
  <Row {...props} className={classNames(props.className, 'Row-right')}/>;

export const RowMiddle = (props: LayoutProps) =>
  <Row {...props} className={classNames(props.className, 'Row-middle')}/>;

export const RowBottom = (props: LayoutProps) =>
  <Row {...props} className={classNames(props.className, 'Row-bottom')}/>;

export const RowIndented = (props: LayoutProps) =>
  <Row {...props} className={classNames(props.className, 'Row-indented')}/>;

export const RowSpaceBetween = (props: LayoutProps) =>
  <Row {...props} className={classNames(props.className, 'Row-space-between')}/>;

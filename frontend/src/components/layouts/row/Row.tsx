import {default as classNames} from 'classnames';
import * as React from 'react';
import {LayoutProps} from '../layout/Layout';
import './Row.scss';

export const Row = ({children, className, onClick, style, title}: LayoutProps) => (
  <div className={classNames('Row', className)} onClick={onClick} style={style} title={title}>
    {children}
  </div>
);

export const RowCenter = ({className, ...props}: LayoutProps) =>
  <Row {...props} className={classNames(className, 'Row-center')}/>;

export const RowRight = ({className, ...props}: LayoutProps) =>
  <Row {...props} className={classNames(className, 'Row-right')}/>;

export const RowLeft = ({className, ...props}: LayoutProps) =>
  <Row {...props} className={classNames(className, 'Row-left')}/>;

export const RowMiddle = ({className, ...props}: LayoutProps) =>
  <Row {...props} className={classNames(className, 'Row-middle')}/>;

export const RowBottom = ({className, ...props}: LayoutProps) =>
  <Row {...props} className={classNames(className, 'Row-bottom')}/>;

export const RowSpaceBetween = ({className, ...props}: LayoutProps) =>
  <Row {...props} className={classNames(className, 'Row-space-between')}/>;

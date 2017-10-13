import * as classNames from 'classnames';
import * as React from 'react';
import {LayoutProps} from '../layout/Layout';
import './Column.scss';

export const Column = (props: LayoutProps) => {
  return (
    <div className={classNames('Column', props.className)} onClick={props.onClick}>
      {props.children}
    </div>
  );
};
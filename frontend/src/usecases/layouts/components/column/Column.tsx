import * as classNames from 'classnames';
import * as React from 'react';
import {LayoutProps} from '../layoutTypes';
import './Column.scss';

export const Column = (props: LayoutProps) => {
  return (
    <div className={classNames('Column', props.className)}>
      {props.children}
    </div>
  );
};

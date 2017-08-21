import * as classNames from 'classnames';
import * as React from 'react';
import './Column.scss';

export const Column = (props) => {
  return (
    <div className={classNames('Column', props.className)}>
      {props.children}
    </div>
  );
};

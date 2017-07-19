import * as React from 'react';
import './Column.scss';

export const Column = (props) => {
  return (
    <div className={`Column ${props.className || ''}`}>
      {props.children}
    </div>
  );
};

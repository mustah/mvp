import * as classNames from 'classnames';
import * as React from 'react';
import './Row.scss';

export const Row = (props) => {
  const {className} = props;
  return (
    <div className={classNames('Row', className)}>
      {props.children}
    </div>
  );
};

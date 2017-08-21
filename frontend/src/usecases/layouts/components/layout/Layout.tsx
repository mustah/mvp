import * as classNames from 'classnames';
import * as React from 'react';
import './Layout.scss';

export const Layout = (props) => {
  return (
    <div className={classNames('Layout', props.className)}>
      {props.children}
    </div>
  );
};

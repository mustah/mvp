import * as React from 'react';
import './Layout.scss';

export const Layout = (props) => {
  return (
    <div className="Layout">
      {props.children}
    </div>
  );
};

import * as classNames from 'classnames';
import * as React from 'react';
import './Layout.scss';

export interface LayoutProps {
  hide?: boolean;
  className?: string;
  children?: any;
  onClick?: (...args) => any;
}

export const Layout: React.StatelessComponent<LayoutProps> = (props) => {
  if (props.hide) {
    return null;
  }
  return (
    <div className={classNames('Layout', props.className)}>
      {props.children}
    </div>
  );
};

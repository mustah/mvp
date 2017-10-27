import * as classNames from 'classnames';
import * as React from 'react';
import './Layout.scss';

export interface LayoutProps {
  hide?: boolean;
  className?: string;
  children?: React.ReactNode;
  onClick?: (...args) => void;
}

export const Layout = (props: LayoutProps) => {
  if (props.hide) {
    return null;
  }
  return (
    <div className={classNames('Layout', props.className)}>
      {props.children}
    </div>
  );
};

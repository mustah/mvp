import * as classNames from 'classnames';
import * as React from 'react';
import './Layout.scss';

export interface AllLayoutProps {
  hide: boolean;
  className: string;
  style: React.CSSProperties;
  children: React.ReactNode;
  onClick: (...args) => void;
}

export type LayoutProps = Partial<AllLayoutProps>;

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

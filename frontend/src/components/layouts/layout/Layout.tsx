import * as classNames from 'classnames';
import 'Layout.scss';
import * as React from 'react';
import {Children, OnClick} from '../../../types/Types';

interface AllLayoutProps {
  hide: boolean;
  className: string;
  style: React.CSSProperties;
  children: Children;
  onClick: OnClick;
}

export type LayoutProps = Partial<AllLayoutProps>;

export const Layout = ({hide = false, children, className, style}: LayoutProps) => {
  if (hide) {
    return null;
  }
  return (
    <div className={classNames('Layout', className)} style={style}>
      {children}
    </div>
  );
};

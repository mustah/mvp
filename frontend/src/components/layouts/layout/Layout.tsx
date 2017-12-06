import * as classNames from 'classnames';
import 'Layout.scss';
import * as React from 'react';
import {Children, OnClick} from '../../../types/Types';

export interface AllLayoutProps {
  hide: boolean;
  className: string;
  style: React.CSSProperties;
  children: Children;
  onClick: OnClick;
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

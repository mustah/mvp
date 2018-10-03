import {default as classNames} from 'classnames';
import './Layout.scss';
import * as React from 'react';
import {ClassNamed, Clickable, Styled, WithChildren} from '../../../types/Types';

export type LayoutProps = Partial<WithChildren & Clickable & ClassNamed & Styled>;

export const Layout = ({children, className, style}: LayoutProps) => (
  <div className={classNames('Layout', className)} style={style}>
    {children}
  </div>
);

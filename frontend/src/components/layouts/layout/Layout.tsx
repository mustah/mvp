import {default as classNames} from 'classnames';
import * as React from 'react';
import {ClassNamed, ClickableEventHandler, Styled, Titled, WithChildren} from '../../../types/Types';
import './Layout.scss';

export type LayoutProps = Partial<WithChildren & ClickableEventHandler & ClassNamed & Styled & Titled>;

export const Layout = ({children, className, style, title}: LayoutProps) => (
  <div className={classNames('Layout', className)} style={style} title={title}>
    {children}
  </div>
);

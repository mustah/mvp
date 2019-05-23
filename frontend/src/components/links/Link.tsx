import * as React from 'react';
import {classes} from 'typestyle';
import {ClassNamed, Styled, WithChildren} from '../../types/Types';

type FrameName = string;

export interface LinkProps extends ClassNamed, Styled, WithChildren {
  to: string;
  target?: '_blank' | '_self' | '_parent' | '_top' | FrameName;
}

export const Link = ({children, className, style, target, to}: LinkProps) => (
  <a
    className={classes('link', className)}
    href={target === '_blank' ? to : `/#${to}`}
    target={target}
    style={style}
  >
    {children}
  </a>
);

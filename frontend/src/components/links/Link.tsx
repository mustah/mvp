import * as React from 'react';
import {classes} from 'typestyle';
import {ClassNamed, Styled, WithChildren} from '../../types/Types';
import {ThemeContext, withCssStyles} from '../hoc/withThemeProvider';

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

export const StyledLink = withCssStyles(({cssStyles: {primary}, ...linkProps}: LinkProps & ThemeContext) =>
  <Link {...linkProps} style={{color: primary.bg}}/>);

import * as React from 'react';
import {labelStyle} from '../../../../app/themes';
import {ThemeContext, withCssStyles} from '../../../../components/hoc/withThemeProvider';
import {WithChildren} from '../../../../types/Types';

interface Props extends ThemeContext, WithChildren {
  href: string;
}

export const LinkTo = withCssStyles(({href, children, cssStyles: {primary}}: Props) => (
  <a href={`#${href}`} className="link" style={{...labelStyle, color: primary.bg}}>
    {children}
  </a>
));

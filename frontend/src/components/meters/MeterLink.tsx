import * as React from 'react';
import {routes} from '../../app/routes';
import {Identifiable} from '../../types/Types';
import {ThemeContext, withCssStyles} from '../hoc/withThemeProvider';

interface Props extends Identifiable, ThemeContext {
  facility: string;
  subPath?: string;
}

const labelStyle: React.CSSProperties = {
  textOverflow: 'ellipsis',
  maxWidth: 170,
  whiteSpace: 'nowrap',
  overflow: 'hidden',
  paddingLeft: 14,
};

export const MeterLink = withCssStyles(({facility, id, subPath = '', cssStyles: {primary}}: Props) => (
  <a href={`#${routes.meter}/${id}${subPath}`} className="link" style={{...labelStyle, color: primary.bg}}>
    {facility.toString()}
  </a>
));

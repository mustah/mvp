import * as React from 'react';
import {ThemeContext, withCssStyles} from '../../../components/hoc/withThemeProvider';
import {Row} from '../../../components/layouts/row/Row';
import './MenuUnderline.scss';

export const MenuUnderline = withCssStyles(({cssStyles: {primary}}: ThemeContext) =>
  <Row className="MenuUnderline" style={{backgroundColor: primary.bg}}/>
);

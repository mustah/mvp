import CircularProgress from 'material-ui/CircularProgress';
import * as React from 'react';
import {ThemeContext, withCssStyles} from '../hoc/withThemeProvider';

const loadingStyle: React.CSSProperties = {
  marginTop: 100,
  marginBottom: 100,
};

export const LoadingLarge = withCssStyles(({cssStyles: {primary}}: ThemeContext) => (
  <CircularProgress
    size={60}
    thickness={4}
    style={loadingStyle}
    color={primary.bg}
  />
));

const smallLoadingStyle: React.CSSProperties = {
  marginBottom: 4,
};

export const LoadingSmall = withCssStyles(({cssStyles: {primary}}: ThemeContext) => (
  <CircularProgress
    size={24}
    thickness={2}
    style={smallLoadingStyle}
    color={primary.bg}
  />
));

const widgetLoadingStyle: React.CSSProperties = {
  marginTop: 30,
  marginBottom: 30,
  height: 58,
};

export const LoadingWidget = withCssStyles(({cssStyles: {primary}}: ThemeContext) => (
  <CircularProgress
    size={24}
    thickness={2}
    style={widgetLoadingStyle}
    color={primary.bg}
  />
));

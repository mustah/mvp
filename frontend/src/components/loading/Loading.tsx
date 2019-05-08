import CircularProgress from 'material-ui/CircularProgress';
import * as React from 'react';
import {colors} from '../../app/colors';

const loadingStyle: React.CSSProperties = {
  marginTop: 100,
  marginBottom: 100,
};

export const LoadingLarge = () => (
  <CircularProgress
    size={60}
    thickness={4}
    style={loadingStyle}
    color={colors.blueA700}
  />
);

const smallLoadingStyle: React.CSSProperties = {
  marginBottom: 4,
};

export const LoadingSmall = () => (
  <CircularProgress
    size={24}
    thickness={2}
    style={smallLoadingStyle}
    color={colors.blueA700}
  />
);
const widgetLoadingStyle: React.CSSProperties = {
  marginTop: 30,
  marginBottom: 30,
  height: 58,
};

export const LoadingWidget = () => (
  <CircularProgress
    size={24}
    thickness={2}
    style={widgetLoadingStyle}
    color={colors.blueA700}
  />
);

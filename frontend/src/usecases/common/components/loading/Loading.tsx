import CircularProgress from 'material-ui/CircularProgress';
import * as React from 'react';
import {colors} from '../../../app/themes';

const loadingStyle: React.CSSProperties = {
  marginTop: 100,
  marginBottom: 100,
};

export const Loading = () => <CircularProgress size={60} thickness={4} style={loadingStyle} color={colors.blue}/>;

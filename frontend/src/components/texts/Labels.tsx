import * as React from 'react';
import {ThemeContext, withCssStyles} from '../hoc/withThemeProvider';
import {Column} from '../layouts/column/Column';
import {BoldFirstUpper, FirstUpper} from './Texts';

interface Props extends ThemeContext {
  name: string;
  subTitle: string;
}

const style: React.CSSProperties = {fontSize: 11, fontWeight: 'normal'};

export const CityInfo = withCssStyles(({cssStyles: {primary}, name, subTitle}: Props) => (
  <Column>
    <BoldFirstUpper>{name}</BoldFirstUpper>
    <FirstUpper style={{...style, color: primary.fg}}>{subTitle}</FirstUpper>
  </Column>
));

export const LabelWithSubtitle = withCssStyles(({cssStyles: {primary}, name, subTitle}: Props) => (
  <Column>
    <FirstUpper>{name}</FirstUpper>
    <FirstUpper style={{...style, color: primary.fg}}>{subTitle}</FirstUpper>
  </Column>
));

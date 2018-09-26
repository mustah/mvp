import * as React from 'react';
import {colors} from '../../app/themes';
import {Medium} from '../indicators/indicatorWidgetModels';
import {iconComponentFor} from '../indicators/ReportIndicatorWidget';

interface OwnProps {
  medium: Medium;
  style?: React.CSSProperties;
}

export const IconIndicator = ({medium, style}: OwnProps) => {
  const Icon = iconComponentFor(medium);
  return (
    <Icon style={style} className="Indicator-icon" color={colors.lightBlack}/>
  );
};

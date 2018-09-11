import * as React from 'react';
import {colors} from '../../app/themes';
import {getMediumType} from '../indicators/indicatorWidgetModels';
import {iconComponentFor} from '../indicators/ReportIndicatorWidget';

interface OwnProps {
  medium: string;
  style?: React.CSSProperties;
}

export const IconIndicator = ({medium, style}: OwnProps) => {
  const Icon = iconComponentFor(getMediumType(medium));
  return (
    <Icon style={style} className="Indicator-icon" color={colors.lightBlack}/>
  );
};

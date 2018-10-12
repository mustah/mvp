import * as React from 'react';
import {colors} from '../../app/themes';
import {Medium} from '../../state/ui/graph/measurement/measurementModels';
import {iconComponentFor} from '../../usecases/report/components/indicators/ReportIndicatorWidget';

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

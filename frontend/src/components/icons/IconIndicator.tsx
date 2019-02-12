import * as React from 'react';
import {colors} from '../../app/themes';
import {Medium} from '../../state/ui/graph/measurement/measurementModels';
import {mediumIconComponent} from '../../usecases/report/reportModels';

interface OwnProps {
  medium: Medium;
  style?: React.CSSProperties;
}

export const IconIndicator = ({medium, style}: OwnProps) => {
  const Icon = mediumIconComponent(medium);
  return (
    <Icon style={style} className="Indicator-icon" color={colors.lightBlack}/>
  );
};

import IconButton from 'material-ui/IconButton';
import * as React from 'react';
import {colors} from '../../app/themes';
import {Clickable} from '../../types/Types';
import {getMediumType} from '../indicators/indicatorWidgetModels';
import {iconComponentFor} from '../indicators/ReportIndicatorWidget';

const style: React.CSSProperties = {
  width: '12px',
  height: '12px',
};

interface OwnProps {
  medium: string;
}

export const MediumButton = ({medium, onClick}: OwnProps & Clickable) => {
  const IndicatorIcon = iconComponentFor(getMediumType(medium));

  return (
    <IconButton onClick={onClick}>
      <IndicatorIcon style={style} className="Indicator-icon" color={colors.lightBlack}/>
    </IconButton>
  );
};

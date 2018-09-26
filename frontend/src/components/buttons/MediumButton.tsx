import IconButton from 'material-ui/IconButton';
import * as React from 'react';
import {Clickable} from '../../types/Types';
import {IconIndicator} from '../icons/IconIndicator';
import {Medium} from '../indicators/indicatorWidgetModels';

interface OwnProps {
  medium: Medium;
}

export const MediumButton = ({medium, onClick}: OwnProps & Clickable) => (
  <IconButton onClick={onClick}>
    <IconIndicator medium={medium}/>
  </IconButton>
);

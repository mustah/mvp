import IconButton from 'material-ui/IconButton';
import * as React from 'react';
import {Medium} from '../../state/ui/graph/measurement/measurementModels';
import {Clickable} from '../../types/Types';
import {IconIndicator} from '../icons/IconIndicator';

interface OwnProps {
  medium: Medium;
}

export const MediumButton = ({medium, onClick}: OwnProps & Clickable) => (
  <IconButton onClick={onClick} className="MediumButton">
    <IconIndicator medium={medium}/>
  </IconButton>
);

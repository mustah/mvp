import IconButton from 'material-ui/IconButton';
import * as React from 'react';
import {Clickable} from '../../types/Types';
import {IconIndicator} from '../icons/IconIndicator';

interface OwnProps {
  medium: string;
}

export const MediumButton = ({medium, onClick}: OwnProps & Clickable) => {
  return (
    <IconButton onClick={onClick}>
      <IconIndicator medium={medium} />
    </IconButton>
  );
};

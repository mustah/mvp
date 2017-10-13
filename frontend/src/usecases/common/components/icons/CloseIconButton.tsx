import IconButton from 'material-ui/IconButton';
import * as React from 'react';
import {Icon} from './Icons';

interface OwnProps {
  onClick?: (...args) => any;
}

export const CloseIconButton = (props: OwnProps) => (
  <IconButton onClick={props.onClick}>
    <Icon name="close" size="large" className="Icon-Button"/>
  </IconButton>
);

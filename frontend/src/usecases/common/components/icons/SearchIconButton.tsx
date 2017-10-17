import IconButton from 'material-ui/IconButton';
import * as React from 'react';
import {Icon} from './Icons';

interface SearchIconButtonProps {
  onClick?: (...args) => any;
}

export const SearchIconButton = (props: SearchIconButtonProps) => (
  <IconButton onClick={props.onClick}>
    <Icon name="filter-variant" size="large" className="Icon-Button"/>
  </IconButton>
);

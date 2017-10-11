import IconButton from 'material-ui/IconButton';
import ActionSearch from 'material-ui/svg-icons/action/search';
import * as React from 'react';

interface SearchIconButtonProps {
  onClick?: (...args) => any;
}

export const SearchIconButton = (props: SearchIconButtonProps) => (
  <IconButton onClick={props.onClick}>
    <ActionSearch/>
  </IconButton>
);

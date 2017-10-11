import IconButton from 'material-ui/IconButton';
import NavigationMenu from 'material-ui/svg-icons/navigation/menu';
import * as React from 'react';

interface NavigationMenuIconProps {
  disabled?: boolean;
  color?: string;
  onClick?: (...args) => void;
}

export const NavigationMenuIcon = (props: NavigationMenuIconProps) => (
  <IconButton onClick={props.onClick} disabled={props.disabled}>
    <NavigationMenu color={props.color || 'white'}/>
  </IconButton>
);

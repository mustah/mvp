import IconButton from 'material-ui/IconButton';
import NavigationMenu from 'material-ui/svg-icons/navigation/menu';
import * as React from 'react';

interface NavigationMenuIconProps {
  disabled?: boolean;
  isOpen?: boolean;
  color?: string;
  onClick?: (...args) => void;
}

export const NavigationMenuIcon = (props: NavigationMenuIconProps) => (
  <IconButton
    onClick={props.onClick}
    disabled={props.disabled}
    style={{display: props.isOpen ? 'none' : 'inline-block'}}
  >
    <NavigationMenu color={props.color || 'white'}/>
  </IconButton>
);

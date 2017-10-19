import IconButton from 'material-ui/IconButton';
import NavigationMenu from 'material-ui/svg-icons/navigation/menu';
import * as React from 'react';
import {Clickable} from '../../../../types/Types';

interface OwnProps extends Clickable {
  disabled?: boolean;
  isOpen?: boolean;
  color?: string;
}

export const IconNavigationMenu = (props: OwnProps) => (
  <IconButton
    onClick={props.onClick}
    disabled={props.disabled}
    style={{display: props.isOpen ? 'none' : 'inline-block'}}
  >
    <NavigationMenu color={props.color || 'white'}/>
  </IconButton>
);

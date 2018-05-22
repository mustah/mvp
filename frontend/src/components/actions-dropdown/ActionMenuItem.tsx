import MenuItem from 'material-ui/MenuItem';
import * as React from 'react';
import {menuItemInnerDivStyle} from '../../app/themes';
import {OnClick} from '../../types/Types';

export interface ActionMenuItemProps {
  name: string;
  onClick?: OnClick;
}

export const ActionMenuItem = ({name, onClick}: ActionMenuItemProps) => (
  <MenuItem
    key={name}
    style={menuItemInnerDivStyle}
    className="first-uppercase"
    onClick={onClick}
  >
    {name}
  </MenuItem>
);

import MenuItem from 'material-ui/MenuItem';
import * as React from 'react';
import {menuItemInnerDivStyle} from '../../app/themes';
import {OnClickEventHandler} from '../../types/Types';
import MenuItemProps = __MaterialUI.Menus.MenuItemProps;

export interface ActionMenuItemProps extends MenuItemProps {
  name: string;
  onClick: OnClickEventHandler;
}

export const ActionMenuItem = ({leftIcon, name, onClick}: ActionMenuItemProps) => (
  <MenuItem
    key={name}
    leftIcon={leftIcon}
    style={menuItemInnerDivStyle}
    innerDivStyle={leftIcon ? {paddingLeft: 32} : {}}
    className="first-uppercase"
    onClick={onClick}
  >
    {name}
  </MenuItem>
);

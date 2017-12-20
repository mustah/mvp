import MenuItem from 'material-ui/MenuItem';
import * as React from 'react';
import {IconMore} from '../icons/IconMore';
import {PopoverMenu} from '../popover/PopoverMenu';
import Divider = __MaterialUI.Divider;

interface Props {
  menuItems: MenuItems;
  className?: string;
}

export type MenuItems = Array<React.ReactElement<MenuItem | Divider>>;

export const ActionsDropdown = ({menuItems, className}: Props) => (
  <PopoverMenu className={className} IconComponent={IconMore}>
    {menuItems}
  </PopoverMenu>
);

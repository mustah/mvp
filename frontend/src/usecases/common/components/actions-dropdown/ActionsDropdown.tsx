import MenuItem from 'material-ui/MenuItem';
import * as React from 'react';
import {OnClick} from '../../../../types/Types';
import {menuItemInnerDivStyle} from '../../../app/themes';
import {PopoverMenu} from '../popover/PopoverMenu';
import Divider = __MaterialUI.Divider;

interface CallbackAction {
  name: string;
  onClick: OnClick;
}

interface Props {
  menuItems: MenuItems;
  className?: string;
}

export type MenuItems = Array<React.ReactElement<MenuItem | Divider>>;

export const menuItem = (callbackAction: CallbackAction): React.ReactElement<MenuItem> => {
  const {name, onClick} = callbackAction;
  return (
    <MenuItem
      key={name}
      style={menuItemInnerDivStyle}
      className="first-uppercase"
      onClick={onClick}
    >
      {name}
    </MenuItem>
  );
};

export const ActionsDropdown = (props: Props) => {
  const {menuItems, className} = props;

  return (
    <PopoverMenu className={className}>
      {menuItems}
    </PopoverMenu>
  );
};

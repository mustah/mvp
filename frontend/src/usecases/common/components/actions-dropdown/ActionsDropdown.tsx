import MenuItem from 'material-ui/MenuItem';
import * as React from 'react';
import {menuItemInnerDivStyle} from '../../../app/themes';
import {PopoverMenu} from '../popover/PopoverMenu';

interface Props {
  actions: string[];
  className?: string;
}

export const ActionsDropdown = (props: Props) => {
  const {actions, className} = props;

  const renderActions = (action: string, index: number) => (
    <MenuItem key={index} style={menuItemInnerDivStyle} className="first-uppercase">
      {action}
    </MenuItem>
  );

  return (
    <PopoverMenu className={className}>
      {actions.map(renderActions)}
    </PopoverMenu>
  );
};

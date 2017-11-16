import MenuItem from 'material-ui/MenuItem';
import * as React from 'react';
import {OnClick} from '../../../../types/Types';
import {menuItemInnerDivStyle} from '../../../app/themes';
import {PopoverMenu} from '../popover/PopoverMenu';

export interface CallbackAction {
  name: string;
  onClick: OnClick;
}

interface Props {
  actions: CallbackAction[];
  className?: string;
}

export const ActionsDropdown = (props: Props) => {
  const {actions, className} = props;

  const renderActions = (action: CallbackAction, index: number) => (
    <MenuItem
      key={index}
      style={menuItemInnerDivStyle}
      className="first-uppercase"
      onClick={action.onClick}
    >
      {action.name}
    </MenuItem>
  );

  return (
    <PopoverMenu className={className}>
      {actions.map(renderActions)}
    </PopoverMenu>
  );
};

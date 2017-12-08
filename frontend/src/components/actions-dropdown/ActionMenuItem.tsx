import MenuItem from 'material-ui/MenuItem';
import * as React from 'react';
import {menuItemInnerDivStyle} from '../../app/themes';
import {OnClick} from '../../types/Types';

interface Props {
  name: string;
  onClick: OnClick;
}

export const ActionMenuItem = ({name, onClick}: Props) => {
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

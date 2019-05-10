import {DropDownMenu as MaterialDropDownMenu} from 'material-ui/DropDownMenu';
import * as React from 'react';
import {colors} from '../../../../app/colors';
import {WithChildren} from '../../../../types/Types';
import DropDownMenuProps = __MaterialUI.Menus.DropDownMenuProps;
import origin = __MaterialUI.propTypes.origin;

const anchorOrigin: origin = {horizontal: 'left', vertical: 'bottom'};
const targetOrigin: origin = {horizontal: 'left', vertical: 'top'};

const selectedMenuItemStyle: React.CSSProperties = {
  fontWeight: 'bold',
};

const iconStyle: React.CSSProperties = {
  color: colors.black,
  fill: colors.black
};

export const DropDownMenu = ({children, ...props}: DropDownMenuProps & WithChildren) => (
  <MaterialDropDownMenu
    anchorOrigin={anchorOrigin}
    targetOrigin={targetOrigin}
    selectedMenuItemStyle={selectedMenuItemStyle}
    iconStyle={iconStyle}
    {...props}
  >
    {children}
  </MaterialDropDownMenu>
);

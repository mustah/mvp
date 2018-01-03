import MenuItem from 'material-ui/MenuItem';
import * as React from 'react';
import {Link} from 'react-router-dom';
import {routes} from '../../app/routes';
import {colors} from '../../app/themes';
import {translate} from '../../services/translationService';
import {PopoverMenu} from '../popover/PopoverMenu';
import origin = __MaterialUI.propTypes.origin;
import NavigationMenu from 'material-ui/svg-icons/navigation/menu';
import 'AppSwitchDropdown.scss';

export const AppSwitchDropdown = () => {

  const MenuIcon = ({onClick}) => <NavigationMenu color={colors.white} onClick={onClick}/>;
  const anchorOrigin: origin = {horizontal: 'right', vertical: 'top'};
  const targetOrigin: origin = {horizontal: 'middle', vertical: 'bottom'};

  return (
    <PopoverMenu
      IconComponent={MenuIcon}
      anchorOrigin={anchorOrigin}
      targetOrigin={targetOrigin}
      className="popover clickable"
    >
      <Link to={routes.home} className="link">
        <MenuItem className="first-uppercase">
          {translate('metering')}
        </MenuItem>
      </Link>
      <Link to={routes.admin} className="link">
        <MenuItem className="first-uppercase">
          {translate('admin')}
        </MenuItem>
      </Link>
    </PopoverMenu>
  );
};

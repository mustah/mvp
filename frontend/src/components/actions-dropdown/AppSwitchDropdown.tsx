import MenuItem from 'material-ui/MenuItem';
import NavigationMenu from 'material-ui/svg-icons/navigation/menu';
import * as React from 'react';
import {Route, Switch} from 'react-router';
import {Link} from 'react-router-dom';
import {routes} from '../../app/routes';
import {colors} from '../../app/themes';
import {translate} from '../../services/translationService';
import {RenderFunction} from '../../types/Types';
import {RowCenter} from '../layouts/row/Row';
import {PopoverMenu} from '../popover/PopoverMenu';
import {Xsmall} from '../texts/Texts';
import origin = __MaterialUI.propTypes.origin;

export const AppSwitchDropdown = () => {

  const renderAdmin = () => translate('admin');
  const renderMetering = () => translate('metering');

  const MenuIcon = ({onClick}) => (
    <RowCenter onClick={onClick} className="MenuItem clickable">
      <NavigationMenu color={colors.white}/>
      <Xsmall className="Bold first-uppercase">
        <Switch>
          <Route path={routes.admin} render={renderAdmin}/>
          <Route path={routes.home} render={renderMetering}/>
        </Switch>
      </Xsmall>
    </RowCenter>
  );

  const renderPopoverContent: RenderFunction = () => ([(
      <Link to={routes.home} className="link" key="mvp">
        <MenuItem className="first-uppercase">
          {translate('metering')}
        </MenuItem>
      </Link>), (
      < Link to={routes.admin} className="link" key="admin">
        <MenuItem className="first-uppercase">
          {translate('admin')}
        </MenuItem>
      </Link>)]
  );

  const anchorOrigin: origin = {horizontal: 'right', vertical: 'top'};
  const targetOrigin: origin = {horizontal: 'middle', vertical: 'bottom'};

  return (
    <PopoverMenu
      IconComponent={MenuIcon}
      anchorOrigin={anchorOrigin}
      targetOrigin={targetOrigin}
      renderPopoverContent={renderPopoverContent}
    />
  );
};

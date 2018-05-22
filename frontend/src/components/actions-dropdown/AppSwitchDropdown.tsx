import MenuItem from 'material-ui/MenuItem';
import NavigationMenu from 'material-ui/svg-icons/navigation/menu';
import * as React from 'react';
import {Route, Switch} from 'react-router';
import {Link} from 'react-router-dom';
import {routes} from '../../app/routes';
import {colors} from '../../app/themes';
import {translate} from '../../services/translationService';
import {OnClick, RenderFunction} from '../../types/Types';
import {ColumnCenter} from '../layouts/column/Column';
import {PopoverMenu} from '../popover/PopoverMenu';
import {Xsmall} from '../texts/Texts';
import origin = __MaterialUI.propTypes.origin;

const anchorOrigin: origin = {horizontal: 'right', vertical: 'top'};
const targetOrigin: origin = {horizontal: 'middle', vertical: 'bottom'};
const appSwitchIconStyle: React.CSSProperties = {width: '100%', alignItems: 'center'};

const renderAdmin = () => translate('admin');

const renderMetering = () => translate('metering');

const MenuIcon = ({onClick}) => (
  <ColumnCenter onClick={onClick} className="MenuItem clickable" style={appSwitchIconStyle}>
    <NavigationMenu color={colors.white}/>
    <Xsmall className="Bold first-uppercase">
      <Switch>
        <Route path={routes.admin} render={renderAdmin}/>
        <Route path={routes.home} render={renderMetering}/>
      </Switch>
    </Xsmall>
  </ColumnCenter>
);

const EvoMenuItem = () => (
  <Link to={routes.home} className="link" key="mvp">
    <MenuItem className="first-uppercase">
      {translate('metering')}
    </MenuItem>
  </Link>
);

const AdminMenuItemLink = () => (
  <Link to={routes.admin} className="link" key="admin">
    <MenuItem className="first-uppercase">
      {translate('admin')}
    </MenuItem>
  </Link>
);

export const AppSwitchDropdown = () => {
  const renderPopoverContent: RenderFunction<OnClick> = () => ([
      <EvoMenuItem key="evo-pages"/>,
      <AdminMenuItemLink key="admin-pages"/>,
    ]
  );

  return (
    <PopoverMenu
      IconComponent={MenuIcon}
      anchorOrigin={anchorOrigin}
      targetOrigin={targetOrigin}
      renderPopoverContent={renderPopoverContent}
    />
  );
};

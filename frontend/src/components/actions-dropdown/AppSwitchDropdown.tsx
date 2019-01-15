import MenuItem from 'material-ui/MenuItem';
import NavigationMenu from 'material-ui/svg-icons/navigation/menu';
import * as React from 'react';
import {Route, Switch} from 'react-router';
import {Link} from 'react-router-dom';
import {routes} from '../../app/routes';
import {colors, menuItemInnerDivStyle} from '../../app/themes';
import {translate} from '../../services/translationService';
import {Clickable, OnClick, RenderFunction} from '../../types/Types';
import {ColumnCenter} from '../layouts/column/Column';
import {anchorOrigin, PopoverMenu, targetOrigin} from '../popover/PopoverMenu';
import {Xsmall} from '../texts/Texts';

const appSwitchIconStyle: React.CSSProperties = {width: '100%', alignItems: 'center'};

const renderAdmin = () => translate('admin');

const renderMetering = () => translate('metering');

const MenuIcon = ({onClick}: Clickable) => (
  <ColumnCenter onClick={onClick} className="MenuItem clickable" style={appSwitchIconStyle}>
    <NavigationMenu color={colors.white}/>
    <Xsmall className="uppercase">
      <Switch>
        <Route path={routes.admin} render={renderAdmin}/>
        <Route path={routes.home} render={renderMetering}/>
      </Switch>
    </Xsmall>
  </ColumnCenter>
);

interface Props {
  text: string;
  to: string;
}

const LinkMenuItem = ({text, to}: Props) => (
  <Link to={to} className="link">
    <MenuItem className="first-uppercase" style={menuItemInnerDivStyle}>{text}</MenuItem>
  </Link>
);

export const AppSwitchDropdown = () => {
  const renderPopoverContent: RenderFunction<OnClick> = () => ([
      <LinkMenuItem to={routes.home} text={translate('metering')} key="metering-pages"/>,
      <LinkMenuItem to={routes.admin} text={translate('admin')} key="admin-pages"/>,
    ]
  );

  return (
    <PopoverMenu
      className="AppSwitchDropdown"
      IconComponent={MenuIcon}
      anchorOrigin={anchorOrigin}
      targetOrigin={targetOrigin}
      renderPopoverContent={renderPopoverContent}
    />
  );
};

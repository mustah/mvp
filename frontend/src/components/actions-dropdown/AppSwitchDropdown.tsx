import MenuItem from 'material-ui/MenuItem';
import NavigationMenu from 'material-ui/svg-icons/navigation/menu';
import * as React from 'react';
import {Route, Switch} from 'react-router';
import {Link} from 'react-router-dom';
import {routes} from '../../app/routes';
import {colors, menuItemInnerDivStyle} from '../../app/themes';
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
      IconComponent={MenuIcon}
      anchorOrigin={anchorOrigin}
      targetOrigin={targetOrigin}
      renderPopoverContent={renderPopoverContent}
    />
  );
};

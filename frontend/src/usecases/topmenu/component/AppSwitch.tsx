import MenuItem from 'material-ui/MenuItem';
import ActionDashboard from 'material-ui/svg-icons/action/dashboard';
import NavigationApps from 'material-ui/svg-icons/navigation/apps';
import * as React from 'react';
import {Link, Route, Switch} from 'react-router-dom';
import {routes} from '../../../app/routes';
import {colors, iconStyle, topMenuInnerDivStyle, topMenuItemDivStyle, topMenuItemIconStyle} from '../../../app/themes';
import {IconMeter} from '../../../components/icons/IconMeter';
import {ColumnCenter} from '../../../components/layouts/column/Column';
import {anchorOrigin, PopoverMenu, targetOrigin} from '../../../components/popover/PopoverMenu';
import {firstUpperTranslated, translate} from '../../../services/translationService';
import {Clickable, OnClick, RenderFunction, WithChildren} from '../../../types/Types';
import {TopMenuItem} from './TopMenuItem';

interface Props extends Clickable, WithChildren {
  leftIcon?: React.ReactElement<any>;
  to: string;
}

const LinkMenuItem = ({children, leftIcon, to, onClick}: Props) => (
  <Link to={to} className="link">
    <MenuItem
      className="first-uppercase"
      leftIcon={leftIcon}
      innerDivStyle={topMenuInnerDivStyle}
      onClick={onClick}
      style={topMenuItemDivStyle}
    >
      {children}
    </MenuItem>
  </Link>
);

const MenuIcon = ({onClick}: Clickable) => (
  <ColumnCenter onClick={onClick}>
    <NavigationApps color={colors.white} style={iconStyle}/>
    <Switch>
      <Route path={routes.admin}/>
      <Route path={routes.home}/>
    </Switch>
  </ColumnCenter>
);

export const AppSwitch = () => {
  const renderPopoverContent: RenderFunction<OnClick> = (onClick: OnClick) => ([
      (
        <LinkMenuItem
          onClick={onClick}
          to={routes.home}
          leftIcon={<IconMeter style={topMenuItemIconStyle}/>}
          key="metering-pages"
        >
          {translate('metering')}
        </LinkMenuItem>
      ),
      (
        <LinkMenuItem
          onClick={onClick}
          to={routes.admin}
          leftIcon={<ActionDashboard style={topMenuItemIconStyle}/>}
          key="admin-pages"
        >
          {translate('admin')}
        </LinkMenuItem>
      ),
    ]
  );

  return (
    <TopMenuItem title={firstUpperTranslated('switch to')}>
      <PopoverMenu
        className="Row-center"
        IconComponent={MenuIcon}
        anchorOrigin={anchorOrigin}
        targetOrigin={targetOrigin}
        renderPopoverContent={renderPopoverContent}
      />
    </TopMenuItem>
  );
};

import ActionDashboard from 'material-ui/svg-icons/action/dashboard';
import NavigationApps from 'material-ui/svg-icons/navigation/apps';
import * as React from 'react';
import {Route, Switch} from 'react-router-dom';
import {routes} from '../../../app/routes';
import {colors, iconStyle, topMenuItemIconStyle} from '../../../app/themes';
import {IconMeter} from '../../../components/icons/IconMeter';
import {ColumnCenter} from '../../../components/layouts/column/Column';
import {anchorOrigin, PopoverMenu, targetOrigin} from '../../../components/popover/PopoverMenu';
import {firstUpperTranslated, translate} from '../../../services/translationService';
import {Clickable, OnClick, RenderFunction} from '../../../types/Types';
import {LinkMenuItem} from './LinkMenuItem';
import {TopMenuItem} from './TopMenuItem';

const MenuIcon = ({onClick}: Clickable) => (
  <ColumnCenter onClick={onClick}>
    <NavigationApps color={colors.white} style={iconStyle}/>
    <Switch>
      <Route path={routes.admin}/>
      <Route path={routes.home}/>
    </Switch>
  </ColumnCenter>
);

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

export const AppSwitch = () => (
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

import ActionDashboard from 'material-ui/svg-icons/action/dashboard';
import NavigationApps from 'material-ui/svg-icons/navigation/apps';
import * as React from 'react';
import {colors} from '../../../app/colors';
import {routes} from '../../../app/routes';
import {iconStyle, topMenuItemIconStyle} from '../../../app/themes';
import {IconMeter} from '../../../components/icons/IconMeter';
import {ColumnCenter} from '../../../components/layouts/column/Column';
import {anchorOrigin, PopoverMenu, targetOrigin} from '../../../components/popover/PopoverMenu';
import {firstUpperTranslated, translate} from '../../../services/translationService';
import {Clickable, OnClick, RenderFunction} from '../../../types/Types';
import {TopMenuItem} from './TopMenuItem';
import {TopMenuLinkItem} from './TopMenuLinkItem';

const MenuIcon = ({onClick}: Clickable) => (
  <ColumnCenter onClick={onClick}>
    <NavigationApps color={colors.white} style={iconStyle}/>
  </ColumnCenter>
);

const renderPopoverContent: RenderFunction<OnClick> = (onClick: OnClick) => ([
    (
      <TopMenuLinkItem
        onClick={onClick}
        to={routes.home}
        leftIcon={<IconMeter style={topMenuItemIconStyle}/>}
        key="metering-pages"
      >
        {translate('metering')}
      </TopMenuLinkItem>
    ),
    (
      <TopMenuLinkItem
        onClick={onClick}
        to={routes.admin}
        leftIcon={<ActionDashboard style={topMenuItemIconStyle}/>}
        key="admin-pages"
      >
        {translate('admin')}
      </TopMenuLinkItem>
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

import ActionDashboard from 'material-ui/svg-icons/action/dashboard';
import ActionImportantDevices from 'material-ui/svg-icons/action/important-devices';
import NavigationApps from 'material-ui/svg-icons/navigation/apps';
import * as React from 'react';
import {colors} from '../../../app/colors';
import {routes} from '../../../app/routes';
import {iconStyle, topMenuItemIconStyle} from '../../../app/themes';
import {withMvpAdminOnly, withMvpUser, withOtcWebUser} from '../../../components/hoc/withRoles';
import {IconMeter} from '../../../components/icons/IconMeter';
import {ColumnCenter} from '../../../components/layouts/column/Column';
import {anchorOrigin, PopoverMenu, targetOrigin} from '../../../components/popover/PopoverMenu';
import {StoreProvider} from '../../../components/popover/StoreProvider';
import {firstUpperTranslated, translate} from '../../../services/translationService';
import {Clickable, OnClick, RenderFunction} from '../../../types/Types';
import {TopMenuItem} from './TopMenuItem';
import {TopMenuLinkItem} from './TopMenuLinkItem';

const MenuIcon = ({onClick}: Clickable) => (
  <ColumnCenter onClick={onClick}>
    <NavigationApps color={colors.white} style={iconStyle}/>
  </ColumnCenter>
);

const MvpTopMenuLinkItem = withMvpUser<OnClick>(() => (
  <TopMenuLinkItem
    to={routes.home}
    leftIcon={<IconMeter style={topMenuItemIconStyle}/>}
  >
    {translate('metering')}
  </TopMenuLinkItem>
));

const AdminTopMenuLinkItem = withMvpAdminOnly<OnClick>(() => (
  <TopMenuLinkItem
    to={routes.admin}
    leftIcon={<ActionDashboard style={topMenuItemIconStyle}/>}
  >
    {translate('admin')}
  </TopMenuLinkItem>
));

const OtcTopMenuLinkItem = withOtcWebUser<OnClick>(() => (
  <TopMenuLinkItem
    to={routes.otc}
    leftIcon={<ActionImportantDevices style={topMenuItemIconStyle}/>}
  >
    {translate('otc')}
  </TopMenuLinkItem>
));

const renderPopoverContent: RenderFunction<OnClick> = () => ([
    (
      <StoreProvider key="metering-pages">
        <MvpTopMenuLinkItem/>
      </StoreProvider>
    ),
    (
      <StoreProvider key="admin-pages">
        <AdminTopMenuLinkItem/>
      </StoreProvider>
    ),
    (
      <StoreProvider key="otc-web-pages">
        <OtcTopMenuLinkItem/>
      </StoreProvider>
    )
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

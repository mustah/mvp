import MenuItem from 'material-ui/MenuItem';
import ActionDashboard from 'material-ui/svg-icons/action/dashboard';
import NavigationApps from 'material-ui/svg-icons/navigation/apps';
import * as React from 'react';
import {Route, Switch} from 'react-router';
import {Link} from 'react-router-dom';
import {routes} from '../../../app/routes';
import {colors, topMenuInnerDivStyle, topMenuItemDivStyle, topMenuItemIconStyle} from '../../../app/themes';
import {IconMeter} from '../../../components/icons/IconMeter';
import {ColumnCenter} from '../../../components/layouts/column/Column';
import {anchorOrigin, PopoverMenu, targetOrigin} from '../../../components/popover/PopoverMenu';
import {Xsmall} from '../../../components/texts/Texts';
import {translate} from '../../../services/translationService';
import {Clickable, OnClick, RenderFunction} from '../../../types/Types';
import './AppSwitch.scss';
import {MenuUnderline} from './MenuUnderline';

const appSwitchIconStyle: React.CSSProperties = {width: '100%', alignItems: 'center'};

const renderAdmin = () => translate('admin');

const renderMetering = () => translate('metering');

const MenuIcon = ({onClick}: Clickable) => (
  <ColumnCenter onClick={onClick} style={appSwitchIconStyle}>
    <NavigationApps color={colors.white}/>
    <Xsmall className="uppercase">
      <Switch>
        <Route path={routes.admin} render={renderAdmin}/>
        <Route path={routes.home} render={renderMetering}/>
      </Switch>
    </Xsmall>
  </ColumnCenter>
);

interface Props extends Clickable {
  leftIcon?: React.ReactElement<any>;
  text: string;
  to: string;
}

const LinkMenuItem = ({leftIcon, text, to, onClick}: Props) => (
  <Link to={to} className="link">
    <MenuItem
      leftIcon={leftIcon}
      className="first-uppercase"
      innerDivStyle={topMenuInnerDivStyle}
      style={topMenuItemDivStyle}
      onClick={onClick}
    >
      {text}
    </MenuItem>
  </Link>
);

export const AppSwitch = () => {
  const renderPopoverContent: RenderFunction<OnClick> = (onClick: OnClick) => ([
      (
        <LinkMenuItem
          onClick={onClick}
          to={routes.home}
          text={translate('metering')}
          leftIcon={<IconMeter style={topMenuItemIconStyle}/>}
          key="metering-pages"
        />
      ),
      (
        <LinkMenuItem
          onClick={onClick}
          to={routes.admin}
          text={translate('admin')}
          leftIcon={<ActionDashboard style={topMenuItemIconStyle}/>}
          key="admin-pages"
        />
      ),
    ]
  );

  return (
    <ColumnCenter className="AppSwitch TopMenu-Item">
      <PopoverMenu
        IconComponent={MenuIcon}
        anchorOrigin={anchorOrigin}
        targetOrigin={targetOrigin}
        renderPopoverContent={renderPopoverContent}
      />
      <MenuUnderline/>
    </ColumnCenter>
  );
};

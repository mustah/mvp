import MenuItem from 'material-ui/MenuItem';
import ActionDashboard from 'material-ui/svg-icons/action/dashboard';
import NavigationApps from 'material-ui/svg-icons/navigation/apps';
import * as React from 'react';
import {Route, Switch} from 'react-router';
import {Link} from 'react-router-dom';
import {routes} from '../../../app/routes';
import {actionMenuItemIconStyle, colors, menuItemInnerDivStyle} from '../../../app/themes';
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

interface Props {
  leftIcon?: React.ReactElement<any>;
  text: string;
  to: string;
}

const innerDivStyle: React.CSSProperties = {padding: '0 0 0 38px'};

const LinkMenuItem = ({leftIcon, text, to}: Props) => (
  <Link to={to} className="link">
    <MenuItem
      leftIcon={leftIcon}
      className="first-uppercase"
      innerDivStyle={innerDivStyle}
      style={menuItemInnerDivStyle}
    >
      {text}
    </MenuItem>
  </Link>
);

export const AppSwitch = () => {
  const renderPopoverContent: RenderFunction<OnClick> = () => ([
      (
        <LinkMenuItem
          to={routes.home}
          text={translate('metering')}
          leftIcon={<IconMeter style={actionMenuItemIconStyle} color={colors.black}/>}
          key="metering-pages"
        />
      ),
      (
        <LinkMenuItem
          to={routes.admin}
          text={translate('admin')}
          leftIcon={<ActionDashboard style={actionMenuItemIconStyle} color={colors.black}/>}
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

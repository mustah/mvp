import MenuItem from 'material-ui/MenuItem';
import NavigationMenu from 'material-ui/svg-icons/navigation/menu';
import * as React from 'react';
import {connect} from 'react-redux';
import {Route, Switch} from 'react-router';
import {Link} from 'react-router-dom';
import {routes} from '../../app/routes';
import {colors} from '../../app/themes';
import {RootState} from '../../reducers/rootReducer';
import {isAdminSelector} from '../../services/authService';
import {translate} from '../../services/translationService';
import {OnClick, RenderFunction} from '../../types/Types';
import {ColumnCenter} from '../layouts/column/Column';
import {PopoverMenu} from '../popover/PopoverMenu';
import {Xsmall} from '../texts/Texts';
import origin = __MaterialUI.propTypes.origin;

interface StateToProps {
  isAdmin: boolean;
}

const anchorOrigin: origin = {horizontal: 'right', vertical: 'top'};
const targetOrigin: origin = {horizontal: 'middle', vertical: 'bottom'};

const AppSwitchDropdown = ({isAdmin}: StateToProps) => {

  const renderAdmin = () => translate('admin');
  const renderMetering = () => translate('metering');
  const appSwitchIconStyle: React.CSSProperties = {width: '100%', alignItems: 'center'};

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

  const adminMenuItem = isAdmin ? (
    <Link to={routes.admin} className="link" key="admin">
      <MenuItem className="first-uppercase">
        {translate('admin')}
      </MenuItem>
    </Link>) : null;

  const meteringMenuItem = (
    <Link to={routes.home} className="link" key="mvp">
      <MenuItem className="first-uppercase">
        {translate('metering')}
      </MenuItem>
    </Link>);

  const renderPopoverContent: RenderFunction<OnClick> = () => ([
      meteringMenuItem,
      adminMenuItem,
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

const mapStateToProps = (state: RootState): StateToProps => ({
  isAdmin: isAdminSelector(state),
});

export const AppSwitchDropdownContainer = connect<StateToProps>(mapStateToProps)(AppSwitchDropdown);

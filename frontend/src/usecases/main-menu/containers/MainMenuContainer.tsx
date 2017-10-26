import 'MainMenuContainer.scss';
import * as React from 'react';
import {connect} from 'react-redux';
import {Link} from 'react-router-dom';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {getPathname} from '../../../selectors/routerSelector';
import {translate} from '../../../services/translationService';
import {routes} from '../../app/routes';
import {logout} from '../../auth/authActions';
import {AuthState} from '../../auth/authReducer';
import {IconCollection, IconDashboard, IconReport, IconValidation} from '../../common/components/icons/Icons';
import {Column} from '../../common/components/layouts/column/Column';
import {Profile} from '../../profile/components/Profile';
import {toggleShowHideSideMenu} from '../../sidemenu/sideMenuActions';
import {SideMenuState} from '../../sidemenu/sideMenuReducer';
import {MainNavigationMenu} from '../components/main-navigation-menu/MainNavigationMenu';
import {MenuItem} from '../components/menuitems/MenuItem';

interface StateToProps {
  pathname: string;
  auth: AuthState;
  sideMenu: SideMenuState;
}

interface DispatchToProps {
  logout: () => void;
  toggleShowHideSideMenu: () => void;
}

const MainMenuContainerComponent = (props: StateToProps & DispatchToProps) => {
  const {pathname, auth, sideMenu, toggleShowHideSideMenu, logout} = props;

  if (!auth.isAuthenticated) {
    return null;
  }

  return (
    <Column className="MainMenuContainer">
      <MainNavigationMenu
        isOpen={sideMenu.isOpen}
        toggleShowHideSideMenu={toggleShowHideSideMenu}
      />
      <Column className="MenuItems Column-space-between">
        <Column>
          <Link to={routes.dashboard} className="link">
            <MenuItem
              name={translate('dashboard')}
              isSelected={routes.dashboard === pathname || routes.home === pathname}
              icon={<IconDashboard/>}
            />
          </Link>
          <Link to={routes.collection} className="link">
            <MenuItem
              name={translate('collection')}
              isSelected={routes.collection === pathname}
              icon={<IconCollection/>}
            />
          </Link>
          <Link to={routes.validation} className="link">
            <MenuItem
              name={translate('validation')}
              isSelected={routes.validation === pathname}
              icon={<IconValidation/>}
            />
          </Link>
          <Link to={routes.report} className="link">
            <MenuItem
              name={translate('report')}
              isSelected={routes.report === pathname}
              icon={<IconReport/>}
            />
          </Link>
        </Column>
        <Profile user={auth.user!} logout={logout}/>
      </Column>
    </Column>
  );
};

const mapStateToProps = (state: RootState): StateToProps => {
  const {auth, ui: {sideMenu}} = state;
  return {
    pathname: getPathname(state.routing),
    auth,
    sideMenu,
  };
};

const mapDispatchToProps = (dispatch) => bindActionCreators({
  logout,
  toggleShowHideSideMenu,
}, dispatch);

export const MainMenuContainer =
  connect<StateToProps, DispatchToProps, {}>(mapStateToProps, mapDispatchToProps)(MainMenuContainerComponent);

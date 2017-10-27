import 'MainMenuContainer.scss';
import * as React from 'react';
import {connect} from 'react-redux';
import {Link} from 'react-router-dom';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {getPathname} from '../../../selectors/routerSelectors';
import {translate} from '../../../services/translationService';
import {isSideMenuOpen} from '../../../state/ui/uiSelectors';
import {routes} from '../../app/routes';
import {logout} from '../../auth/authActions';
import {AuthState} from '../../auth/authReducer';
import {IconCollection} from '../../common/components/icons/IconCollection';
import {IconDashboard} from '../../common/components/icons/IconDashboard';
import {IconReport} from '../../common/components/icons/IconReport';
import {IconValidation} from '../../common/components/icons/IconValidation';
import {Column} from '../../common/components/layouts/column/Column';
import {Profile} from '../../profile/components/Profile';
import {toggleShowHideSideMenu} from '../../sidemenu/sideMenuActions';
import {MainNavigationMenu} from '../components/main-navigation-menu/MainNavigationMenu';
import {MenuItem} from '../components/menuitems/MenuItem';

interface StateToProps {
  pathname: string;
  auth: AuthState;
  isSideMenuOpen: boolean;
}

interface DispatchToProps {
  logout: () => void;
  toggleShowHideSideMenu: () => void;
}

const MainMenuContainerComponent = (props: StateToProps & DispatchToProps) => {
  const {pathname, auth, isSideMenuOpen, toggleShowHideSideMenu, logout} = props;

  if (!auth.isAuthenticated) {
    return null;
  }

  return (
    <Column className="MainMenuContainer">
      <MainNavigationMenu
        isOpen={isSideMenuOpen}
        toggleShowHideSideMenu={toggleShowHideSideMenu}
      />
      <Column className="MenuItems Column-space-between">
        <Column>
          <Link to={routes.dashboard} className="link">
            <MenuItem
              name={translate('dashboard')}
              isSelected={routes.dashboard === pathname || routes.home === pathname}
              icon={<IconDashboard className="MenuItem-icon"/>}
            />
          </Link>
          <Link to={routes.collection} className="link">
            <MenuItem
              name={translate('collection')}
              isSelected={routes.collection === pathname}
              icon={<IconCollection className="MenuItem-icon"/>}
            />
          </Link>
          <Link to={routes.validation} className="link">
            <MenuItem
              name={translate('validation')}
              isSelected={routes.validation === pathname}
              icon={<IconValidation className="MenuItem-icon"/>}
            />
          </Link>
          <Link to={routes.report} className="link">
            <MenuItem
              name={translate('report')}
              isSelected={routes.report === pathname}
              icon={<IconReport className="MenuItem-icon"/>}
            />
          </Link>
        </Column>
        <Profile user={auth.user!} logout={logout}/>
      </Column>
    </Column>
  );
};

const mapStateToProps = ({auth, routing, ui}: RootState): StateToProps => {
  return {
    pathname: getPathname(routing),
    auth,
    isSideMenuOpen: isSideMenuOpen(ui),
  };
};

const mapDispatchToProps = (dispatch) => bindActionCreators({
  logout,
  toggleShowHideSideMenu,
}, dispatch);

export const MainMenuContainer =
  connect<StateToProps, DispatchToProps, {}>(mapStateToProps, mapDispatchToProps)(MainMenuContainerComponent);

import 'MainMenuContainer.scss';
import * as React from 'react';
import {connect} from 'react-redux';
import {Link} from 'react-router-dom';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/index';
import {translate} from '../../../services/translationService';
import {routes} from '../../app/routes';
import {logout} from '../../auth/authActions';
import {AuthState} from '../../auth/authReducer';
import {IconCollection, IconDashboard, IconReport, IconValidation} from '../../common/components/icons/Icons';
import {Column} from '../../common/components/layouts/column/Column';
import {toggleShowHideSideMenu} from '../../sidemenu/sideMenuActions';
import {SideMenuState} from '../../sidemenu/sideMenuReducer';
import {MainNavigationMenu} from '../components/main-navigation-menu/MainNavigationMenu';
import {MenuItem} from '../components/menuitems/MenuItem';

interface TopMenuContainerProps {
  pathname: string;
  auth: AuthState;
  logout: () => any;
  toggleShowHideSideMenu: () => any;
  sideMenu: SideMenuState;
}

const MainMenuContainerComponent = (props: TopMenuContainerProps) => {
  const {pathname, auth, sideMenu, toggleShowHideSideMenu} = props;
  return (
    <Column className="MainMenuContainer">
      <MainNavigationMenu
        isOpen={sideMenu.isOpen}
        disabled={!auth.isAuthenticated}
        toggleShowHideSideMenu={toggleShowHideSideMenu}
      />
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
  );
};

const mapStateToProps = (state: RootState) => {
  const {
    routing: {location},
    auth,
    language: {language},
    ui: {sideMenu},
  } = state;

  return {
    pathname: location!.pathname,
    auth,
    language,
    sideMenu,
  };
};

const mapDispatchToProps = (dispatch) => bindActionCreators({
  logout,
  toggleShowHideSideMenu,
}, dispatch);

export const MainMenuContainer = connect(mapStateToProps, mapDispatchToProps)(MainMenuContainerComponent);

import * as React from 'react';
import {connect} from 'react-redux';
import {Link} from 'react-router-dom';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/index';
import {routes} from '../../app/routes';
import {logout} from '../../auth/authActions';
import {AuthState} from '../../auth/authReducer';
import {Row} from '../../common/components/layouts/row/Row';
import {ProfileContainer} from '../../profile/containers/ProfileContainer';
import {toggleShowHideSideMenu} from '../../sidemenu/sideMenuActions';
import {SideMenuState} from '../../sidemenu/sideMenuReducer';
import {MainNavigationMenu} from '../components/main-navigation-menu/MainNavigationMenu';
import {MenuItem} from '../components/menuitems/MenuItem';

export interface TopMenuContainerProps {
  pathname: string;
  auth: AuthState;
  logout: () => any;
  toggleShowHideSideMenu: () => any;
  sideMenu: SideMenuState;
}

const TopMenuContainer = (props: TopMenuContainerProps) => {
  const {pathname, auth, logout, sideMenu, toggleShowHideSideMenu} = props;
  return (
    <Row className="flex-1">
      <MainNavigationMenu
        isOpen={sideMenu.isOpen}
        disabled={!auth.isAuthenticated}
        toggleShowHideSideMenu={toggleShowHideSideMenu}
      />
      <Row>
        <Link to={routes.dashboard} className="link">
          <MenuItem
            name="Dashboard"
            isSelected={routes.dashboard === pathname || routes.home === pathname}
            icon="dialpad"
          />
        </Link>
        <Link to={routes.collection} className="link">
          <MenuItem name="Insamling" isSelected={routes.collection === pathname} icon="dialpad"/>
        </Link>
        <Link to={routes.validation} className="link">
          <MenuItem name="Validering" isSelected={routes.validation === pathname} icon="dialpad"/>
        </Link>
        <Link to={routes.report} className="link">
          <MenuItem name="Rapport" isSelected={routes.report === pathname} icon="dialpad"/>
        </Link>
      </Row>
      <ProfileContainer user={auth.user} logout={logout}/>
    </Row>
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

export default connect(mapStateToProps, mapDispatchToProps)(TopMenuContainer);

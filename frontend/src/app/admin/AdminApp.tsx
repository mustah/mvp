import {default as classNames} from 'classnames';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history3/redirect';
import {withSideMenu} from '../../components/hoc/withSideMenu';
import {Row} from '../../components/layouts/row/Row';
import {MessageContainer} from '../../containers/MessageContainer';
import {OnClick} from '../../types/Types';
import {MainMenuToggleIcon} from '../../usecases/main-menu/components/menu-items/MainMenuToggleIcon';
import {AdminMainMenuItemsContainer} from '../../usecases/main-menu/containers/AdminMainMenuItemsContainer';
import {SideMenuContainer} from '../../usecases/sidemenu/containers/SideMenuContainer';
import {toggleShowHideSideMenu} from '../../usecases/sidemenu/sideMenuActions';
import {AdminPages} from './AdminPages';

interface StateToProps {
  isSideMenuOpen: boolean;
}

interface DispatchToProps {
  toggleShowHideSideMenu: OnClick;
}

type Props = StateToProps & DispatchToProps & InjectedAuthRouterProps;

const AdminAppComponent = ({isSideMenuOpen, toggleShowHideSideMenu}: Props) => (
  <Row>
    <SideMenuContainer className={classNames({isSideMenuOpen})}>
      <AdminMainMenuItemsContainer/>
    </SideMenuContainer>
    <MainMenuToggleIcon onClick={toggleShowHideSideMenu} isSideMenuOpen={isSideMenuOpen}/>
    <AdminPages/>
    <MessageContainer/>
  </Row>
);

const mapDispatchToProps = (dispatch) => bindActionCreators({
  toggleShowHideSideMenu,
}, dispatch);

const AdminApp = withSideMenu<Props>(AdminAppComponent);

export const AdminAppContainer =
  connect<StateToProps, DispatchToProps, Props>(null, mapDispatchToProps)(AdminApp);

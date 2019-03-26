import {default as classNames} from 'classnames';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history3/redirect';
import {Layout} from '../../components/layouts/layout/Layout';
import {Row} from '../../components/layouts/row/Row';
import {MessageContainer} from '../../containers/MessageContainer';
import {RootState} from '../../reducers/rootReducer';
import {isSideMenuOpen} from '../../state/ui/uiSelectors';
import {OnClick} from '../../types/Types';
import {MainMenuToggleIcon} from '../../usecases/main-menu/components/menu-items/MainMenuToggleIcon';
import {AdminMainMenuItemsContainer} from '../../usecases/main-menu/containers/AdminMainMenuItemsContainer';
import {SideMenuContainer} from '../../usecases/sidemenu/containers/SideMenuContainer';
import {toggleShowHideSideMenu} from '../../usecases/sidemenu/sideMenuActions';
import './AdminApp.scss';
import {AdminPages} from './AdminPages';

interface StateToProps {
  isSideMenuOpen: boolean;
}

interface DispatchToProps {
  toggleShowHideSideMenu: OnClick;
}

type Props = StateToProps & DispatchToProps & InjectedAuthRouterProps;

const AdminApp = ({isSideMenuOpen, toggleShowHideSideMenu}: Props) => (
  <Row className="AdminApp">
    <Layout className={classNames('SideMenuContainer', {isSideMenuOpen})}>
      <SideMenuContainer>
        <AdminMainMenuItemsContainer/>
      </SideMenuContainer>
    </Layout>
    <MainMenuToggleIcon onClick={toggleShowHideSideMenu} isSideMenuOpen={isSideMenuOpen}/>
    <AdminPages/>
    <MessageContainer/>
  </Row>
);

const mapStateToProps = ({ui}: RootState) => ({
  isSideMenuOpen: isSideMenuOpen(ui),
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  toggleShowHideSideMenu,
}, dispatch);

export const AdminAppContainer =
  connect<StateToProps, DispatchToProps, Props>(mapStateToProps, mapDispatchToProps)(AdminApp);

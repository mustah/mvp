import 'AdminApp.scss';
import * as classNames from 'classnames';
import AppBar from 'material-ui/AppBar';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history3/redirect';
import {Layout} from '../../components/layouts/layout/Layout';
import {Row} from '../../components/layouts/row/Row';
import {RootState} from '../../reducers/rootReducer';
import {translate} from '../../services/translationService';
import {isSideMenuOpen} from '../../state/ui/uiSelectors';
import {OnClick} from '../../types/Types';
import {MainMenuToggleIcon} from '../../usecases/main-menu/components/menuitems/MainMenuToggleIcon';
import {AdminMainMenuContainer} from '../../usecases/main-menu/containers/AdminMainMenuContainer';
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

const AdminApp = ({isSideMenuOpen, toggleShowHideSideMenu}: Props) => {

  return (
    <Row className="AdminApp">
      <AdminMainMenuContainer/>
      <Layout className={classNames('SideMenuContainer', {isSideMenuOpen})}>
        <SideMenuContainer>
          <AppBar
            className="AppTitle"
            title={translate('admin')}
            showMenuIconButton={false}
          />
        </SideMenuContainer>
      </Layout>
      <MainMenuToggleIcon onClick={toggleShowHideSideMenu} isSideMenuOpen={isSideMenuOpen}/>
      <AdminPages/>
    </Row>
  );
};

const mapStateToProps = ({ui}: RootState) => ({
  isSideMenuOpen: isSideMenuOpen(ui),
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  toggleShowHideSideMenu,
}, dispatch);

export const AdminAppContainer =
  connect<StateToProps, DispatchToProps, Props>(mapStateToProps, mapDispatchToProps)(AdminApp);

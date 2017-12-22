import * as React from 'react';
import {connect} from 'react-redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {Layout} from '../../components/layouts/layout/Layout';
import {Row} from '../../components/layouts/row/Row';
import {RootState} from '../../reducers/rootReducer';
import {isSideMenuOpen} from '../../state/ui/uiSelectors';
import {MainMenuContainer} from '../../usecases/main-menu/containers/MainMenuContainer';
import {SideMenuContainer} from '../../usecases/sidemenu/containers/SideMenuContainer';
import {MvpPages} from './MvpPages';
import * as classNames from 'classnames';
import './MvpApp.scss';

interface StateToProps {
  isAuthenticated: boolean;
  isSideMenuOpen: boolean;
}

type Props = StateToProps & InjectedAuthRouterProps;

const MvpApp = ({isAuthenticated, isSideMenuOpen}: Props) => {
  return (
    <Row className="MvpApp">
      <MainMenuContainer/>

      <Layout className={classNames('SideMenuContainer', {isSideMenuOpen})} hide={!isAuthenticated}>
        <SideMenuContainer/>
      </Layout>

      <MvpPages/>
    </Row>
  );
};

const mapStateToProps = ({auth: {isAuthenticated}, ui}: RootState) => ({
  isAuthenticated,
  isSideMenuOpen: isSideMenuOpen(ui),
});

export const MvpAppContainer = connect<StateToProps, {}, Props>(mapStateToProps)(MvpApp);

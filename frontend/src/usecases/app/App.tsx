import * as classNames from 'classnames';
import * as React from 'react';
import {connect} from 'react-redux';
import {withRouter} from 'react-router';
import {RootState} from '../../reducers/index';
import {AuthState} from '../auth/authReducer';
import {Layout} from '../common/components/layouts/layout/Layout';
import {Row} from '../common/components/layouts/row/Row';
import SideMenuContainer from '../sidemenu/containers/SideMenuContainer';
import {SideMenuState} from '../sidemenu/sideMenuReducer';
import TopMenuContainer from '../topmenu/containers/TopMenuContainer';
import './_common.scss';
import './App.scss';
import {Pages} from './Pages';

/**
 * The Application root component should extend React.Component in order
 * for HMR (hot module reloading) to work properly. Otherwise, prefer
 * functional components.
 */
class App extends React.Component<{auth: AuthState, sideMenu: SideMenuState}, any> {
  render() {
    const {isAuthenticated} = this.props.auth;
    const {isOpen} = this.props.sideMenu;

    return (
      <div className="App">
        <TopMenuContainer/>
        <Row>
          <Layout className={classNames('SideMenuContainer', {isOpen})} hide={!isAuthenticated}>
            <SideMenuContainer/>
          </Layout>
          <Pages/>
        </Row>
      </div>
    );
  }
}

const mapStateToProps = (state: RootState) => {
  const {auth, ui: {sideMenu}} = state;
  return {
    auth,
    sideMenu,
  };
};

export default withRouter(connect(mapStateToProps, {})(App));

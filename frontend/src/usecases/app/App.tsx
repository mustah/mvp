import * as React from 'react';
import {connect} from 'react-redux';
import {withRouter} from 'react-router';
import {Route} from 'react-router-dom';
import {RootState} from '../../reducers/index';
import {userIsAuthenticated, userIsNotAuthenticated} from '../../services/authService';
import LoginContainer from '../auth/containers/LoginContainer';
import CollectionContainer from '../collection/containers/CollectionContainer';
import DashboardContainer from '../dashboard/containers/DashboardContainer';
import DataAnalysisContainer from '../dataAnalysis/containers/DataAnalysisContainer';
import {Layout} from '../layouts/components/layout/Layout';
import {Row} from '../layouts/components/row/Row';
import {SideMenuContainer} from '../sidemenu/containers/SideMenuContainer';
import TopMenuContainer from '../topmenu/containers/TopMenuContainer';
import ValidationContainer from '../validation/containers/ValidationContainer';
import './_common.scss';
import './App.scss';
import {routes} from './routes';

const LoginPage = userIsNotAuthenticated(LoginContainer);
const DashboardPage = userIsAuthenticated(DashboardContainer);
const CollectionPage = userIsAuthenticated(CollectionContainer);
const ValidationPage = userIsAuthenticated(ValidationContainer);
const DataAnalysisPage = userIsAuthenticated(DataAnalysisContainer);

/**
 * The Application root component should extend React.Component in order
 * for HMR (hot module reloading) to work properly. Otherwise, prefer
 * functional components.
 */
class App extends React.Component<RootState, any> {

  render() {
    const {isAuthenticated} = this.props.auth;
    return (
      <div className="App">
        <Row>
          <Layout>
            <TopMenuContainer/>
          </Layout>
        </Row>
        <Row>
          <Layout className="side-menu-container" hide={!isAuthenticated}>
            <SideMenuContainer/>
          </Layout>
          <Layout>
            <Route path={routes.login} component={LoginPage}/>
            <Route exact={true} path={routes.home} component={DashboardPage}/>
            <Route exact={true} path={routes.dashboard} component={DashboardPage}/>
            <Route exact={true} path={routes.collection} component={CollectionPage}/>
            <Route exact={true} path={routes.validation} component={ValidationPage}/>
            <Route exact={true} path={routes.dataAnalysis} component={DataAnalysisPage}/>
          </Layout>
        </Row>
      </div>
    );
  }
}

const mapStateToProps = (state: RootState) => ({...state});

export default withRouter(connect(mapStateToProps, {})(App));

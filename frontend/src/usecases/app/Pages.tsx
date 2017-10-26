import * as React from 'react';
import {Route} from 'react-router';
import {userIsAuthenticated, userIsNotAuthenticated} from '../../services/authService';
import {LoginContainer} from '../auth/containers/LoginContainer';
import CollectionContainer from '../collection/containers/CollectionContainer';
import {Layout} from '../common/components/layouts/layout/Layout';
import DashboardContainer from '../dashboard/containers/DashboardContainer';
import ReportContainer from '../report/containers/ReportContainer';
import {SelectionContainer} from '../selection/containers/SelectionContainer';
import ValidationContainer from '../validation/containers/ValidationContainer';
import {routes} from './routes';

const LoginPage = userIsNotAuthenticated(LoginContainer);
const DashboardPage = userIsAuthenticated(DashboardContainer);
const CollectionPage = userIsAuthenticated(CollectionContainer);
const ValidationPage = userIsAuthenticated(ValidationContainer);
const ReportPage = userIsAuthenticated(ReportContainer);
const SearchPage = userIsAuthenticated(SelectionContainer);

export const Pages = (props) => (
  <Layout className="flex-1">
    <Route path={routes.login} component={LoginPage}/>
    <Route exact={true} path={routes.home} component={DashboardPage}/>
    <Route exact={true} path={routes.dashboard} component={DashboardPage}/>
    <Route exact={true} path={routes.collection} component={CollectionPage}/>
    <Route exact={true} path={routes.validation} component={ValidationPage}/>
    <Route exact={true} path={routes.report} component={ReportPage}/>
    <Route exact={true} path="/:page/search" component={SearchPage}/>
    <Route exact={true} path="/search" component={SearchPage}/>
  </Layout>
);

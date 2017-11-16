import * as React from 'react';
import {Route, Switch} from 'react-router';
import {userIsAuthenticated, userIsNotAuthenticated} from '../../services/authService';
import {LoginContainer} from '../auth/containers/LoginContainer';
import {Collection} from '../collection/components/Collection';
import {Layout} from '../common/components/layouts/layout/Layout';
import DashboardContainer from '../dashboard/containers/DashboardContainer';
import ReportContainer from '../report/containers/ReportContainer';
import {SelectionContainer} from '../selection/containers/SelectionContainer';
import {Validation} from '../validation/components/Validation';
import {routes} from './routes';

const LoginPage = userIsNotAuthenticated(LoginContainer);
const DashboardPage = userIsAuthenticated(DashboardContainer);
const CollectionPage = userIsAuthenticated(Collection);
const ValidationPage = userIsAuthenticated(Validation);
const ReportPage = userIsAuthenticated(ReportContainer);
const SelectionPage = userIsAuthenticated(SelectionContainer);

export const Pages = (props) => (
  <Layout className="flex-1">
    <Switch>
      <Route path={routes.login} component={LoginPage}/>
      <Route exact={true} path="/:page/selection" component={SelectionPage}/>
      <Route exact={true} path="/:page/:id/selection" component={SelectionPage}/>
      <Route exact={true} path={routes.home} component={DashboardPage}/>
      <Route exact={true} path={routes.dashboard} component={DashboardPage}/>
      <Route exact={true} path={routes.collection} component={CollectionPage}/>
      <Route exact={true} path={routes.validation} component={ValidationPage}/>
      <Route exact={true} path={`${routes.report}/:id`} component={ReportPage}/>
      <Route exact={true} path={routes.report} component={ReportPage}/>
       <Route exact={true} path="/selection" component={SelectionPage}/>
    </Switch>
  </Layout>
);

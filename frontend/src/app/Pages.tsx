import * as React from 'react';
import {Redirect, Route, RouteComponentProps, Switch} from 'react-router';
import {userIsAuthenticated, userIsNotAuthenticated} from '../services/authService';
import {LoginContainer} from '../usecases/auth/containers/LoginContainer';
import {Collection} from '../usecases/collection/components/Collection';
import {Layout} from '../components/layouts/layout/Layout';
import {DashboardContainer} from '../usecases/dashboard/containers/DashboardContainer';
import {ReportContainer} from '../usecases/report/containers/ReportContainer';
import {SelectionContainer} from '../usecases/selection/containers/SelectionContainer';
import {Validation} from '../usecases/validation/components/Validation';
import {companySpecificLogo, routes} from './routes';

const LoginPage = userIsNotAuthenticated(LoginContainer);
const DashboardPage = userIsAuthenticated(DashboardContainer);
const CollectionPage = userIsAuthenticated(Collection);
const ValidationPage = userIsAuthenticated(Validation);
const ReportPage = userIsAuthenticated(ReportContainer);
const SelectionPage = userIsAuthenticated(SelectionContainer);

const renderLogin = ({match: {params: {company}}}: RouteComponentProps<{company: string}>) => (
    companySpecificLogo[company] ? <Route component={LoginPage} /> :
      <Redirect to={routes.login}/>
);

export const Pages = () => (
  <Layout className="flex-1">
    <Switch>
      <Route exact={true} path={routes.login} component={LoginPage} />
      <Route exact={true} path={routes.login + '/:company?'} render={renderLogin} />
      <Route exact={true} path={routes.home} component={DashboardPage}/>
      <Route exact={true} path={routes.dashboard} component={DashboardPage}/>
      <Route exact={true} path={routes.collection} component={CollectionPage}/>
      <Route exact={true} path={routes.validation} component={ValidationPage}/>
      <Route exact={true} path={routes.report + '/:id'} component={ReportPage}/>
      <Route exact={true} path={routes.report} component={ReportPage}/>
      <Route exact={true} path={routes.selection} component={SelectionPage}/>
      <Redirect to={routes.home}/>
    </Switch>
  </Layout>
);

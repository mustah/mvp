import * as React from 'react';
import {Redirect, Route, Switch} from 'react-router';
import {Layout} from '../components/layouts/layout/Layout';
import {userIsAuthenticated, userIsNotAuthenticated} from '../services/authService';
import {LoginContainer} from '../usecases/auth/containers/LoginContainer';
import {AdminAppContainer} from './admin/AdminApp';
import {MvpAppContainer} from './mvp/MvpApp';
import {routes} from './routes';

const LoginPage = userIsNotAuthenticated(LoginContainer);
const MvpPage = userIsAuthenticated(MvpAppContainer);
const AdminPage = userIsAuthenticated(AdminAppContainer);

export const MainPages = () => (
  <Layout className="flex-1">
    <Switch>
      <Route exact={true} path={`${routes.login}/:company?`} component={LoginPage} />
      <Route path={routes.admin} component={AdminPage} />
      <Route path={routes.home} component={MvpPage}/>
      <Redirect to={routes.home}/>
    </Switch>
  </Layout>
);

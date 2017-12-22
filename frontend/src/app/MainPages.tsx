import * as React from 'react';
import {Redirect, Route, Switch} from 'react-router';
import {Layout} from '../components/layouts/layout/Layout';
import {userIsAuthenticated, userIsNotAuthenticated} from '../services/authService';
import {LoginContainer} from '../usecases/auth/containers/LoginContainer';
import {MvpAppContainer} from './mvp/MvpApp';
import {routes} from './routes';

const LoginPage = userIsNotAuthenticated(LoginContainer);
const MvpPage = userIsAuthenticated(MvpAppContainer);
// TODO: const AdminPage = userIsAuthenticated(SelectionContainer);

export const MainPages = () => (
  <Layout className="flex-1">
    <Switch>
      <Route exact={true} path={`${routes.login}/:company?`} component={LoginPage} />
      <Route path={routes.home} component={MvpPage}/>
      <Redirect to={routes.home}/>
    </Switch>
  </Layout>
);

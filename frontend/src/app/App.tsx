import * as React from 'react';
import {Redirect, Route, Switch, withRouter} from 'react-router';
import {adminIsAuthenticated, isAuthenticated, isNotAuthenticated} from '../services/authService';
import {LoginContainer} from '../usecases/auth/containers/LoginContainer';
import {AdminAppContainer} from './admin/AdminAppContainer';
import './App.scss';
import {MvpAppContainer} from './mvp/MvpApp';
import {routes} from './routes';

const LoginPage = isNotAuthenticated(LoginContainer);
const MvpPage = isAuthenticated(MvpAppContainer);
const AdminPage = adminIsAuthenticated(AdminAppContainer);

const AppComponent = () => (
  <Switch>
    <Route exact={true} path={`${routes.login}/:organisation?`} component={LoginPage}/>
    <Route path={routes.admin} component={AdminPage}/>
    <Route path={routes.home} component={MvpPage}/>
    <Redirect to={routes.home}/>
  </Switch>
);

export const App = withRouter(AppComponent);

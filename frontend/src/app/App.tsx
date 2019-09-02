import * as React from 'react';
import {Redirect, Route, Switch, withRouter} from 'react-router';
import {adminIsAuthenticated, isAuthenticated, isNotAuthenticated, otcIsAuthenticated} from '../services/authService';
import {LoginContainer} from '../usecases/auth/containers/LoginContainer';
import {AdminAppContainer} from './admin/AdminAppContainer';
import './App.scss';
import {MvpAppContainer} from './mvp/MvpApp';
import {OtcAppContainer} from './otc/OtcAppContainer';
import {routes} from './routes';

const LoginPage = isNotAuthenticated(LoginContainer);
const AdminPage = adminIsAuthenticated(AdminAppContainer);
const OtcPage = otcIsAuthenticated(OtcAppContainer);
const MvpPage = isAuthenticated(MvpAppContainer);

const AppComponent = () => (
  <Switch>
    <Route exact={true} path={`${routes.login}/:organisation?`} component={LoginPage}/>
    <Route path={routes.admin} component={AdminPage}/>
    <Route path={routes.otc} component={OtcPage}/>
    <Route path={routes.home} component={MvpPage}/>
    <Redirect to={routes.home}/>
  </Switch>
);

export const App = withRouter(AppComponent);

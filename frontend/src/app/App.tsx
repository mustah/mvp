import * as React from 'react';
import {Redirect, Route, Switch, withRouter} from 'react-router';
import {userIsAuthenticated, userIsNotAuthenticated} from '../services/authService';
import {LoginContainer} from '../usecases/auth/containers/LoginContainer';
import {AdminAppContainer} from './admin/AdminApp';
import './App.scss';
import {MvpAppContainer} from './mvp/MvpApp';
import {routes} from './routes';

/**
 * The Application root component should extend React.Component in order
 * for HMR (hot module reloading) to work properly. Otherwise, prefer
 * functional components.
 */

const LoginPage = userIsNotAuthenticated(LoginContainer);
const MvpPage = userIsAuthenticated(MvpAppContainer);
const AdminPage = userIsAuthenticated(AdminAppContainer);

class AppComponent extends React.Component {

  render() {
     return (
       <Switch>
         <Route exact={true} path={`${routes.login}/:company?`} component={LoginPage} />
         <Route path={routes.admin} component={AdminPage} />
         <Route path={routes.home} component={MvpPage}/>
         <Redirect to={routes.home}/>
       </Switch>
     );
  }
}

export const App = withRouter(AppComponent);

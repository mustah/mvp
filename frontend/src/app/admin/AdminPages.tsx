import * as React from 'react';
import {Redirect, Route, Switch} from 'react-router';
import {Administration} from '../../usecases/administration/components/Administration';
import {routes} from '../routes';
import {UserEditContainer} from '../../usecases/administration/containers/UserEditContainer';

export const AdminPages = () => (
  <Switch>
    <Route exact={true} path={routes.admin} component={Administration}/>
    <Route exact={true} path={routes.adminUsers} component={Administration}/>
    <Route exact={true} path={routes.adminUsersAdd} component={UserEditContainer}/>
    <Redirect to={routes.admin}/>
  </Switch>
);

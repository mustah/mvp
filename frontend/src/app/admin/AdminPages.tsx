import * as React from 'react';
import {Redirect, Route, Switch} from 'react-router';
import {Administration} from '../../usecases/administration/components/Administration';
import {UserEditContainer} from '../../usecases/administration/containers/UserEditContainer';
import {routes} from '../routes';
import {UserAddContainer} from '../../usecases/administration/containers/UserAddContainer';

export const AdminPages = () => (
  <Switch>
    <Route exact={true} path={routes.admin} component={Administration}/>
    <Route exact={true} path={routes.adminUsers} component={Administration}/>
    <Route exact={true} path={routes.adminUsersAdd} component={UserAddContainer}/>
    <Route exact={true} path={`${routes.adminUsersModify}/:userId`} component={UserEditContainer} />
    <Redirect to={routes.admin}/>
  </Switch>
);

import * as React from 'react';
import {Redirect, Route, Switch} from 'react-router';
import {Administration} from '../../usecases/administration/components/Administration';
import {routes} from '../routes';

export const AdminPages = () => (
  <Switch>
    <Route exact={true} path={routes.admin} component={Administration}/>
    <Redirect to={routes.admin}/>
  </Switch>
);

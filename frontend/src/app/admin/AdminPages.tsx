import * as React from 'react';
import {Redirect, Route, Switch} from 'react-router';
import {Layout} from '../../components/layouts/layout/Layout';
import {routes} from '../routes';

export const AdminPages = () => (
  <Layout className="flex-1">
    <Switch>
      <Route exact={true} path={routes.admin} component={() => <div>Hej</div>} />
      <Redirect to={routes.admin}/>
    </Switch>
  </Layout>
);

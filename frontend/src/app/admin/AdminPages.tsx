import * as React from 'react';
import {Redirect, Route, Switch} from 'react-router';
import {superAdminIsAuthenticated} from '../../services/authService';
import {Administration} from '../../usecases/administration/components/Administration';
import {
  OrganisationAdministrationContainer,
} from '../../usecases/administration/containers/OrganisationAdministrationContainer';
import {UserEditContainer} from '../../usecases/administration/containers/UserEditContainer';
import {routes} from '../routes';
import {UserAddContainer} from '../../usecases/administration/containers/UserAddContainer';

const OrganisationsPage = superAdminIsAuthenticated(OrganisationAdministrationContainer);

export const AdminPages = () => (
  <Switch>
    <Route exact={true} path={routes.admin} component={Administration}/>
    <Route exact={true} path={routes.adminUsers} component={Administration}/>
    <Route exact={true} path={routes.adminUsersAdd} component={UserAddContainer}/>
    <Route exact={true} path={`${routes.adminUsersModify}/:userId`} component={UserEditContainer}/>
    <Route exact={true} path={routes.adminOrganisations} component={OrganisationsPage}/>
    <Redirect to={routes.admin}/>
  </Switch>
);

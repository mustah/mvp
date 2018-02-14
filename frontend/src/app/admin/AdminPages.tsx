import * as React from 'react';
import {Redirect, Route, Switch} from 'react-router';
import {superAdminIsAuthenticated} from '../../services/authService';
import {Administration} from '../../usecases/administration/components/Administration';
import {OrganisationAddContainer} from '../../usecases/administration/containers/OrganisationAddContainer';
import {
  OrganisationAdministrationContainer,
} from '../../usecases/administration/containers/OrganisationAdministrationContainer';
import {UserEditContainer} from '../../usecases/administration/containers/UserEditContainer';
import {routes} from '../routes';
import {UserAddContainer} from '../../usecases/administration/containers/UserAddContainer';

const OrganisationsPage = superAdminIsAuthenticated(OrganisationAdministrationContainer);
const OrganisationAddPage = superAdminIsAuthenticated(OrganisationAddContainer);

export const AdminPages = () => (
  <Switch>
    <Route exact={true} path={routes.admin} component={Administration}/>
    <Route exact={true} path={routes.adminUsers} component={Administration}/>
    <Route exact={true} path={routes.adminUsersAdd} component={UserAddContainer}/>
    <Route exact={true} path={`${routes.adminUsersModify}/:userId`} component={UserEditContainer}/>
    <Route exact={true} path={routes.adminOrganisations} component={OrganisationsPage}/>
    <Route exact={true} path={routes.adminOrganisationsAdd} component={OrganisationAddPage}/>
    <Redirect to={routes.admin}/>
  </Switch>
);

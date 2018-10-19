import * as React from 'react';
import {Redirect, Route, Switch} from 'react-router';
import {superAdminIsAuthenticated} from '../../services/authService';
import {Users} from '../../usecases/administration/components/Users';
import {OrganisationEditContainer} from '../../usecases/administration/containers/OrganisationEditContainer';
import {Organisations} from '../../usecases/administration/components/Organisations';
import {UserAddContainer} from '../../usecases/administration/containers/UserAddContainer';
import {UserEditContainer} from '../../usecases/administration/containers/UserEditContainer';
import {routes} from '../routes';

const OrganisationsPage = superAdminIsAuthenticated(Organisations);
const OrganisationAddPage = superAdminIsAuthenticated(OrganisationEditContainer);

export const AdminPages = () => (
  <Switch>
    <Route exact={true} path={routes.admin} component={Users}/>
    <Route exact={true} path={routes.adminUsers} component={Users}/>
    <Route exact={true} path={routes.adminUsersAdd} component={UserAddContainer}/>
    <Route exact={true} path={`${routes.adminUsersModify}/:userId`} component={UserEditContainer}/>
    <Route exact={true} path={routes.adminOrganisations} component={OrganisationsPage}/>
    <Route exact={true} path={routes.adminOrganisationsAdd} component={OrganisationAddPage}/>
    <Redirect to={routes.admin}/>
  </Switch>
);

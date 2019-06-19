import * as React from 'react';
import {Redirect, Route, Switch} from 'react-router';
import {mvpAdminIsAuthenticated} from '../../services/authService';
import {Organisations} from '../../usecases/administration/components/Organisations';
import {Users} from '../../usecases/administration/components/Users';
import {OrganisationFormContainer} from '../../usecases/administration/containers/OrganisationFormContainer';
import {UserAddContainer} from '../../usecases/administration/containers/UserAddContainer';
import {UserEditContainer} from '../../usecases/administration/containers/UserEditContainer';
import {MeterDefinitions} from '../../usecases/administration/meter-definitions/components/MeterDefinitions';
import {MeterDefinitionEditContainer} from '../../usecases/administration/meter-definitions/containers/MeterDefinitionEditContainer';
import {routes} from '../routes';

const MeterDefinitionsPage = mvpAdminIsAuthenticated(MeterDefinitions);
const MeterDefinitionEditPage = mvpAdminIsAuthenticated(MeterDefinitionEditContainer);
const OrganisationsPage = mvpAdminIsAuthenticated(Organisations);
const OrganisationPage = mvpAdminIsAuthenticated(OrganisationFormContainer);

export const AdminPages = () => (
  <Switch>
    <Route exact={true} path={routes.admin} component={Users}/>
    <Route exact={true} path={routes.adminUsers} component={Users}/>
    <Route exact={true} path={routes.adminUsersAdd} component={UserAddContainer}/>
    <Route exact={true} path={`${routes.adminUsersModify}/:userId`} component={UserEditContainer}/>
    <Route exact={true} path={routes.adminOrganisations} component={OrganisationsPage}/>
    <Route exact={true} path={routes.adminOrganisationsAdd} component={OrganisationPage}/>
    <Route exact={true} path={`${routes.adminOrganisationsModify}/:organisationId`} component={OrganisationPage}/>
    <Route exact={true} path={routes.adminMeterDefinitions} component={MeterDefinitionsPage}/>
    <Route exact={true} path={routes.adminMeterDefinitionsAdd} component={MeterDefinitionEditPage}/>
    <Route
      exact={true}
      path={`${routes.adminMeterDefinitionsModify}/:meterDefinitionId`}
      component={MeterDefinitionEditPage}
    />
    <Redirect to={routes.admin}/>
  </Switch>
);

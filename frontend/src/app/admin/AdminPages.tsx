import * as React from 'react';
import {Redirect, Route, Switch} from 'react-router';
import {adminIsAuthenticated} from '../../services/authService';
import {MeterDefinitions} from '../../usecases/administration/components/MeterDefinitions';
import {Users} from '../../usecases/administration/components/Users';
import {MeterDefinitionEditContainer} from '../../usecases/administration/containers/MeterDefinitionEditContainer';
import {OrganisationEditContainer} from '../../usecases/administration/containers/OrganisationEditContainer';
import {Organisations} from '../../usecases/administration/components/Organisations';
import {UserAddContainer} from '../../usecases/administration/containers/UserAddContainer';
import {UserEditContainer} from '../../usecases/administration/containers/UserEditContainer';
import {routes} from '../routes';

// TODO why is this not done with users?
const MeterDefinitionsPage = adminIsAuthenticated(MeterDefinitions);
const MeterDefinitionEditPage = adminIsAuthenticated(MeterDefinitionEditContainer);
const OrganisationsPage = adminIsAuthenticated(Organisations);
const OrganisationEditPage = adminIsAuthenticated(OrganisationEditContainer);

export const AdminPages = () => (
  <Switch>
    <Route exact={true} path={routes.admin} component={Users}/>
    <Route exact={true} path={routes.adminUsers} component={Users}/>
    <Route exact={true} path={routes.adminUsersAdd} component={UserAddContainer}/>
    <Route exact={true} path={`${routes.adminUsersModify}/:userId`} component={UserEditContainer}/>
    <Route exact={true} path={routes.adminOrganisations} component={OrganisationsPage}/>
    <Route exact={true} path={routes.adminOrganisationsAdd} component={OrganisationEditPage}/>
    <Route exact={true} path={`${routes.adminOrganisationsModify}/:organisationId`} component={OrganisationEditPage}/>
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

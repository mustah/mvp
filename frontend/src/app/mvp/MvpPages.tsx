import * as React from 'react';
import {Redirect, Route, Switch} from 'react-router';
import {DashboardPage} from '../../usecases/dashboard/containers/DashboardPage';
import {MeterDetailsPage} from '../../usecases/meter/containers/MeterDetailsPage';
import {MetersPage} from '../../usecases/meter/containers/MetersPage';
import {ReportPage} from '../../usecases/report/containers/ReportPage';
import {SearchResultContainer} from '../../usecases/search/containers/SearchResultContainer';
import {SelectionPage} from '../../usecases/selection/containers/SelectionPage';
import {EditProfileContainer} from '../../usecases/topmenu/containers/EditProfileContainer';
import {routes} from '../routes';

export const MvpPages = () => (
  <Switch>
    <Route exact={true} path={routes.home} component={DashboardPage}/>
    <Route exact={true} path={routes.dashboard} component={DashboardPage}/>
    <Route exact={true} path={routes.meters} component={MetersPage}/>
    <Route exact={true} path={`${routes.meter}/:id?`} component={MeterDetailsPage}/>
    <Route exact={true} path={`${routes.meter}/:id?/:collectionPeriod?`} component={MeterDetailsPage}/>
    <Route exact={true} path={`${routes.report}/:id?`} component={ReportPage}/>
    <Route exact={true} path={routes.selection} component={SelectionPage}/>
    <Route exact={true} path={routes.userProfile} component={EditProfileContainer}/>
    <Route exact={true} path={`${routes.searchResult}/:searchQuery`} component={SearchResultContainer}/>
    <Redirect to={routes.home}/>
  </Switch>
);

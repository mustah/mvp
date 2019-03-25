import * as React from 'react';
import {Redirect, Route, Switch} from 'react-router';
import {SingleMeterContainer} from '../../containers/meters/SingleMeterContainer';
import {NewDashboardContainer} from '../../usecases/dashboard/containers/NewDashboardContainer';
import {MetersPage} from '../../usecases/meter/containers/MetersPage';
import {ReportPage} from '../../usecases/report/containers/ReportPage';
import {SearchResultContainer} from '../../usecases/search/containers/SearchResultContainer';
import {SelectionPage} from '../../usecases/selection/containers/SelectionPage';
import {EditProfileContainer} from '../../usecases/topmenu/containers/EditProfileContainer';
import {routes} from '../routes';

export const MvpPages = () => (
  <Switch>
    <Route exact={true} path={routes.home} component={NewDashboardContainer}/>
    <Route exact={true} path={routes.dashboard} component={NewDashboardContainer}/>
    <Route exact={true} path={routes.meter} component={MetersPage}/>
    <Route exact={true} path={`${routes.meter}/:id?`} component={SingleMeterContainer}/>
    <Route exact={true} path={`${routes.meter}/:id?/:collectionPeriod?`} component={SingleMeterContainer}/>
    <Route exact={true} path={`${routes.report}/:id?`} component={ReportPage}/>
    <Route exact={true} path={routes.selection} component={SelectionPage}/>
    <Route exact={true} path={routes.userProfile} component={EditProfileContainer}/>
    <Route exact={true} path={`${routes.searchResult}/:searchQuery`} component={SearchResultContainer}/>
    <Redirect to={routes.home}/>
  </Switch>
);

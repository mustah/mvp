import * as React from 'react';
import {Redirect, Route, Switch} from 'react-router';
import {DashboardContainer} from '../../usecases/dashboard/containers/DashboardContainer';
import {MeterContainer} from '../../usecases/meter/containers/MeterContainer';
import {ReportContainer} from '../../usecases/report/containers/ReportContainer';
import {SelectionContainer} from '../../usecases/selection/containers/SelectionContainer';
import {EditProfileContainer} from '../../usecases/topmenu/containers/EditProfileContainer';
import {routes} from '../routes';

export const MvpPages = () => (
  <Switch>
    <Route exact={true} path={routes.home} component={DashboardContainer}/>
    <Route exact={true} path={routes.dashboard} component={DashboardContainer}/>
    <Route exact={true} path={routes.meter} component={MeterContainer}/>
    <Route exact={true} path={`${routes.report}/:id?`} component={ReportContainer}/>
    <Route exact={true} path={routes.selection} component={SelectionContainer}/>
    <Route exact={true} path={routes.userProfile} component={EditProfileContainer}/>
    <Redirect to={routes.home}/>
  </Switch>
);

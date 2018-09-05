import * as React from 'react';
import {Redirect, Route, Switch} from 'react-router';
import {CollectionContainer} from '../../usecases/collection/components/Collection';
import {DashboardContainer} from '../../usecases/dashboard/containers/DashboardContainer';
import {ReportContainer} from '../../usecases/report/containers/ReportContainer';
import {SelectionContainer} from '../../usecases/selection/containers/SelectionContainer';
import {EditProfileContainer} from '../../usecases/topmenu/containers/EditProfileContainer';
import {ValidationContainer} from '../../usecases/validation/components/Validation';
import {routes} from '../routes';

export const MvpPages = () => (
  <Switch>
    <Route exact={true} path={routes.home} component={DashboardContainer}/>
    <Route exact={true} path={routes.dashboard} component={DashboardContainer}/>
    <Route exact={true} path={routes.collection} component={CollectionContainer}/>
    <Route exact={true} path={routes.validation} component={ValidationContainer}/>
    <Route exact={true} path={`${routes.report}/:id?`} component={ReportContainer}/>
    <Route exact={true} path={routes.selection} component={SelectionContainer}/>
    <Route exact={true} path={routes.userProfile} component={EditProfileContainer}/>
    <Redirect to={routes.home}/>
  </Switch>
);

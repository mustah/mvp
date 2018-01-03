import * as React from 'react';
import {Redirect, Route, Switch} from 'react-router';
import {Collection} from '../../usecases/collection/components/Collection';
import {DashboardContainer} from '../../usecases/dashboard/containers/DashboardContainer';
import {ReportContainer} from '../../usecases/report/containers/ReportContainer';
import {SelectionContainer} from '../../usecases/selection/containers/SelectionContainer';
import {Validation} from '../../usecases/validation/components/Validation';
import {routes} from '../routes';

export const MvpPages = () => (
  <Switch>
    <Route exact={true} path={routes.home} component={DashboardContainer}/>
    <Route exact={true} path={routes.dashboard} component={DashboardContainer}/>
    <Route exact={true} path={routes.collection} component={Collection}/>
    <Route exact={true} path={routes.validation} component={Validation}/>
    <Route exact={true} path={`${routes.report}/:id?`} component={ReportContainer}/>
    <Route exact={true} path={routes.selection} component={SelectionContainer}/>
    <Redirect to={routes.home}/>
  </Switch>
);

import * as React from 'react';
import {Redirect, Route, Switch} from 'react-router';
import {userIsAuthenticated} from '../../services/authService';
import {Collection} from '../../usecases/collection/components/Collection';
import {DashboardContainer} from '../../usecases/dashboard/containers/DashboardContainer';
import {ReportContainer} from '../../usecases/report/containers/ReportContainer';
import {SelectionContainer} from '../../usecases/selection/containers/SelectionContainer';
import {Validation} from '../../usecases/validation/components/Validation';
import {routes} from '../routes';

const DashboardPage = userIsAuthenticated(DashboardContainer);
const CollectionPage = userIsAuthenticated(Collection);
const ValidationPage = userIsAuthenticated(Validation);
const ReportPage = userIsAuthenticated(ReportContainer);
const SelectionPage = userIsAuthenticated(SelectionContainer);

export const MvpPages = () => (
  <Switch>
    <Route exact={true} path={routes.home} component={DashboardPage}/>
    <Route exact={true} path={routes.dashboard} component={DashboardPage}/>
    <Route exact={true} path={routes.collection} component={CollectionPage}/>
    <Route exact={true} path={routes.validation} component={ValidationPage}/>
    <Route exact={true} path={`${routes.report}/:id?`} component={ReportPage}/>
    <Route exact={true} path={routes.selection} component={SelectionPage}/>
    <Redirect to={routes.home}/>
  </Switch>
);

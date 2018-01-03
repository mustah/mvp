import * as React from 'react';
import {Redirect, Route, Switch} from 'react-router';
import {Row} from '../../components/layouts/row/Row';
import {MainTitle} from '../../components/texts/Titles';
import {PageComponent} from '../../containers/PageComponent';
import {routes} from '../routes';

const AdminStartPage = () => (
  <PageComponent isSideMenuOpen={false}>
    <Row>
      <MainTitle>ADMIN PAGE</MainTitle>
    </Row>
  </PageComponent>
);

export const AdminPages = () => (
  <Switch>
    <Route exact={true} path={routes.admin} component={AdminStartPage}/>
    <Redirect to={routes.admin}/>
  </Switch>
);

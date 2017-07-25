import * as React from 'react';
import {Route} from 'react-router-dom';
import CollectionContainer from '../collection/containers/CollectionContainer';
import DashboardContainer from '../dashboard/containers/DashboardContainer';
import {Layout} from '../layouts/components/layout/Layout';
import {Row} from '../layouts/components/row/Row';
import {SideMenuContainer} from '../sidemenu/containers/SideMenuContainer';
import TopMenuContainer from '../topmenu/containers/TopMenuContainer';
import './App.scss';
import {routes} from './routes';

/**
 * The Application root component should extend React.Component in order
 * for HMR to work properly. Otherwise, prefer functional components.
 */
export class App extends React.Component<any, any> {
  render() {
    return (
      <div className="App">
        <Row>
          <Layout>
            <TopMenuContainer/>
          </Layout>
        </Row>
        <Row>
          <Layout className="side-menu-container">
            <SideMenuContainer/>
          </Layout>
          <Layout>
            <Route exact={true} path={routes.home} component={DashboardContainer}/>
            <Route exact={true} path={routes.dashboard} component={DashboardContainer}/>
            <Route exact={true} path={routes.collection} component={CollectionContainer}/>
            <Route exact={true} path={routes.validation} component={DashboardContainer}/>
            <Route exact={true} path={routes.dataAnalysis} component={DashboardContainer}/>
          </Layout>
        </Row>
      </div>
    );
  }
}

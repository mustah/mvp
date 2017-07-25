import * as React from 'react';
import DashboardContainer from '../dashboard/containers/DashboardContainer';
import {Column} from '../layouts/components/column/Column';
import {Layout} from '../layouts/components/layout/Layout';
import {Row} from '../layouts/components/row/Row';
import {SideMenuContainer} from '../sidemenu/containers/SideMenuContainer';
import {TopMenuContainer} from '../topmenu/containers/TopMenuContainer';
import './App.scss';

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
          <Column>
            <Layout className="side-menu-container">
              <SideMenuContainer/>
            </Layout>
          </Column>
          <Column>
            <Layout>
              <DashboardContainer/>
            </Layout>
          </Column>
        </Row>
      </div>
    );
  }
}

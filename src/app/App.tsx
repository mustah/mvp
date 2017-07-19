import * as React from 'react';
import DashboardContainer from '../usecases/dashboard/containers/DashboardContainer';
import {Layout} from '../usecases/layouts/components/layout/Layout';
import {TopMenuContainer} from '../usecases/topmenu/containers/TopMenuContainer';
import './App.scss';

/**
 * The Application root component should extend React.Component in order
 * for HMR to work properly. Otherwise, prefer functional components.
 */
export class App extends React.Component<any, any> {
  render() {
    return (
      <div className="App">
        <Layout>
          <TopMenuContainer/>
        </Layout>
        <Layout>
          <DashboardContainer/>
        </Layout>
      </div>
    );
  }
}

import * as React from 'react';
import {Footer} from '../components/footer/Footer';
import {Column} from '../components/layouts/column/Column';
import {Layout} from '../components/layouts/layout/Layout';
import {WithChildren} from '../types/Types';
import {TopMenu} from '../usecases/topmenu/component/TopMenu';
import {LogoContainer} from '../usecases/topmenu/containers/LogoContainer';
import {GlobalSearchContainer} from './GlobalSearchContainer';

const Content = ({children}: WithChildren) => (
  <Column className="PageContent flex-1">
    {children}
    <Footer/>
  </Column>
);

export const PageComponent = (props: WithChildren) => (
  <Layout className="flex-1">
    <TopMenu>
      <LogoContainer/>
      <GlobalSearchContainer/>
    </TopMenu>
    <Content {...props}/>
  </Layout>
);

export const AdminPageComponent = (props: WithChildren) => (
  <Layout className="flex-1">
    <TopMenu>
      <LogoContainer/>
    </TopMenu>
    <Content {...props}/>
  </Layout>
);

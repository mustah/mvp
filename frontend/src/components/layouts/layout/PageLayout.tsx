import * as React from 'react';
import {GlobalSearchContainer} from '../../../containers/GlobalSearchContainer';
import {WithChildren} from '../../../types/Types';
import {TopMenu} from '../../../usecases/topmenu/component/TopMenu';
import {LogoContainer} from '../../../usecases/topmenu/containers/LogoContainer';
import {Footer} from '../../footer/Footer';
import {Column} from '../column/Column';
import {Layout} from './Layout';

const Content = ({children}: WithChildren) => (
  <Column className="PageContent flex-1">
    {children}
    <Footer/>
  </Column>
);

export const PageLayout = (props: WithChildren) => (
  <Layout className="flex-1">
    <TopMenu>
      <LogoContainer/>
      <GlobalSearchContainer/>
    </TopMenu>
    <Content {...props}/>
  </Layout>
);

export const AdminPageLayout = (props: WithChildren) => (
  <Layout className="flex-1">
    <TopMenu>
      <LogoContainer/>
    </TopMenu>
    <Content {...props}/>
  </Layout>
);

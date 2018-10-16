import * as React from 'react';
import {Footer} from '../components/footer/Footer';
import {Column} from '../components/layouts/column/Column';
import {Layout} from '../components/layouts/layout/Layout';
import {AppTitle} from '../components/texts/Titles';
import {translate} from '../services/translationService';
import {WithChildren} from '../types/Types';
import {TopMenuContainer} from '../usecases/topmenu/containers/TopMenuContainer';

interface Props extends WithChildren {
  topMenuSearch: JSX.Element;
}

const Content = ({children}: WithChildren) => (
  <Column className="PageContent flex-1">
    {children}
    <Footer/>
  </Column>
);

export const PageComponent = ({topMenuSearch, ...props}: Props) => (
  <Layout className="flex-1">
    <TopMenuContainer>
      <AppTitle>{translate('metering')}</AppTitle>
      {topMenuSearch}
    </TopMenuContainer>

    <Content {...props}/>
  </Layout>
);

export const AdminPageComponent = (props: WithChildren) => (
  <Layout className="flex-1">
    <TopMenuContainer>
      <AppTitle>{translate('admin')}</AppTitle>
    </TopMenuContainer>

    <Content {...props}/>
  </Layout>
);

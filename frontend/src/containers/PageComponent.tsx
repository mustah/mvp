import * as classNames from 'classnames';
import * as React from 'react';
import {Footer} from '../components/footer/Footer';
import {Column} from '../components/layouts/column/Column';
import {Layout} from '../components/layouts/layout/Layout';
import {TopMenuWrapperContainer} from '../usecases/topmenu/containers/TopMenuWrapperContainer';

interface Props {
  isSideMenuOpen: boolean;
  children?: React.ReactNode;
  renderTopMenuSearch?: JSX.Element;
}

export const PageComponent = ({children, isSideMenuOpen, renderTopMenuSearch = null}: Props) => (
  <Layout className="flex-1">
    <TopMenuWrapperContainer className={classNames({isSideMenuOpen})}>
      {renderTopMenuSearch}
    </TopMenuWrapperContainer>

    <Column className="PageContent flex-1">
      {children}
      <Footer/>
    </Column>
  </Layout>
);

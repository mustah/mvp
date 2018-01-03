import * as classNames from 'classnames';
import * as React from 'react';
import {Column} from '../components/layouts/column/Column';
import {Layout} from '../components/layouts/layout/Layout';
import {TopMenuWrapperContainer} from '../usecases/topmenu/containers/TopMenuWrapperContainer';

interface Props {
  isSideMenuOpen: boolean;
  children?: React.ReactNode;
  renderTopMenuSearch?: JSX.Element;
}

export const PageComponent = ({children, isSideMenuOpen, renderTopMenuSearch = null}: Props) => {

  return (
    <Layout className="flex-1">
      <TopMenuWrapperContainer className={classNames({isSideMenuOpen})}>
        {renderTopMenuSearch}
      </TopMenuWrapperContainer>

      <Column className="flex-1 PageContent">
        {children}
      </Column>
    </Layout>
  );
};

import {Location} from 'history';
import * as React from 'react';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history3/redirect';
import {PageContainer} from '../../common/components/layouts/layout/PageLayout';
import {SearchContentBox} from '../components/SearchContentBox';

interface SearchContainerProps {
  location: Location;
}

export const SearchContainer = (props: SearchContainerProps & InjectedAuthRouterProps) =>
  (
    <PageContainer>
      <SearchContentBox/>
    </PageContainer>
  );

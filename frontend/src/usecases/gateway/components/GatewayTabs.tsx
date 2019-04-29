import * as React from 'react';
import {MainContentTabs, MainContentTabsProps} from '../../../components/tabs/components/MainContentTabs';
import {GatewayListContainer} from '../containers/GatewayListContainer';

export const GatewayTabs = (props: MainContentTabsProps) => (
  <MainContentTabs {...props}>
    <GatewayListContainer/>
  </MainContentTabs>
);

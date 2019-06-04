import * as React from 'react';
import {MainContentTabs, MainContentTabsProps} from '../../../components/tabs/components/MainContentTabs';
import {MeterListContentContainer} from '../../../containers/MeterListContentContainer';

export const MeterTabs = (props: MainContentTabsProps) => (
  <MainContentTabs {...props}>
    <MeterListContentContainer/>
  </MainContentTabs>
);

import * as React from 'react';
import {MainContentTabs, MainContentTabsProps} from '../../../components/tabs/components/MainContentTabs';
import {MeterListContainer} from '../../../containers/MeterListContainer';
import './MeterTabs.scss';

export const MeterTabs = (props: MainContentTabsProps) => (
  <MainContentTabs {...props} className="MeterTabs">
    <MeterListContainer componentId="validationMeterList"/>
  </MainContentTabs>
);

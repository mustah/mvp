import * as React from 'react';
import {MeterDetailsDialog} from '../../../components/dialog/DetailsDialog';
import {MainContentTabs, MainContentTabsProps} from '../../../components/tabs/components/MainContentTabs';
import {MeterListContainer} from '../../../containers/MeterListContainer';
import './MeterTabs.scss';

export const MeterTabs = (props: MainContentTabsProps) =>
  (
    <MainContentTabs {...props} DetailsDialog={MeterDetailsDialog} className="MeterTabs">
      <MeterListContainer componentId="validationMeterList"/>
    </MainContentTabs>
  );

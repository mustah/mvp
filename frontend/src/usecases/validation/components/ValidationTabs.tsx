import * as React from 'react';
import {MeterDetailsDialog} from '../../../components/dialog/DetailsDialog';
import {
  MainContentTabs,
  MainContentTabsProps,
} from '../../../components/tabs/components/MainContentTabs';
import {MeterListContainer} from '../../../containers/meters/MeterListContainer';

export const ValidationTabs = (props: MainContentTabsProps) =>
  (
    <MainContentTabs {...props} DetailsDialog={MeterDetailsDialog}>
      <MeterListContainer componentId="validationMeterList"/>
    </MainContentTabs>
  );

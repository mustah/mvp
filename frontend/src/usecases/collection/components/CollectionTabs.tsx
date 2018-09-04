import * as React from 'react';
import {GatewayDetailsDialog} from '../../../components/dialog/DetailsDialog';
import {
  MainContentTabs,
  MainContentTabsProps,
} from '../../../components/tabs/components/MainContentTabs';
import {GatewayListContainer} from '../containers/GatewayListContainer';

export const CollectionTabs = (props: MainContentTabsProps) =>
  (
    <MainContentTabs {...props} DetailsDialog={GatewayDetailsDialog}>
      <GatewayListContainer componentId="collectionGatewayList"/>
    </MainContentTabs>
  );

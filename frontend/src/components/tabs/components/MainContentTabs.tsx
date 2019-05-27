import * as React from 'react';
import {translate} from '../../../services/translationService';
import {SelectedTab, TabName} from '../../../state/ui/tabs/tabsModels';
import {CallbackWith, ClassNamed, WithChildren} from '../../../types/Types';
import {CollectionStatContentContainer} from '../../../usecases/collection/containers/CollectionStatContentContainer';
import {MapMarkersContainer} from '../../../usecases/map/containers/MapMarkersContainer';
import {SelectionReportContentContainer} from '../../../usecases/selectionReport/containers/SelectionReportContentContainer';
import {Tab} from './Tab';
import {TabContent} from './TabContent';
import {TabHeaders} from './TabHeaders';
import {Tabs} from './Tabs';
import {TabTopBar} from './TabTopBar';

export interface DispatchToProps {
  changeTab: CallbackWith<TabName>;
}

export type MainContentTabsProps = DispatchToProps & SelectedTab & WithChildren;

export const MainContentTabs = ({
  className,
  children,
  changeTab,
  selectedTab,
}: MainContentTabsProps & ClassNamed) => (
  <Tabs className={className}>
    <TabTopBar>
      <TabHeaders selectedTab={selectedTab} onChangeTab={changeTab}>
        <Tab tab={TabName.list} title={translate('list')}/>
        <Tab tab={TabName.map} title={translate('map')}/>
        <Tab tab={TabName.collection} title={translate('collection')}/>
        <Tab tab={TabName.selectionReport} title={translate('measurements')}/>
      </TabHeaders>
    </TabTopBar>
    <TabContent tab={TabName.list} selectedTab={selectedTab}>
      {children}
    </TabContent>
    <TabContent tab={TabName.map} selectedTab={selectedTab}>
      <MapMarkersContainer/>
    </TabContent>
    <TabContent tab={TabName.collection} selectedTab={selectedTab}>
      {selectedTab === TabName.collection && <CollectionStatContentContainer/>}
    </TabContent>
    <TabContent tab={TabName.selectionReport} selectedTab={selectedTab}>
      {selectedTab === TabName.selectionReport && <SelectionReportContentContainer/>}
    </TabContent>
  </Tabs>
);

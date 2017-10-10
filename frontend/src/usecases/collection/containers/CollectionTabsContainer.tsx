import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/index';
import {translate} from '../../../services/translationService';
import {Image} from '../../common/components/images/Image';
import {Tab} from '../../tabs/components/Tab';
import {TabContent} from '../../tabs/components/TabContent';
import {TabHeaders} from '../../tabs/components/TabHeaders';
import {TabOption} from '../../tabs/components/TabOption';
import {TabOptions} from '../../tabs/components/TabOptions';
import {Tabs} from '../../tabs/components/Tabs';
import {TabSettings} from '../../tabs/components/TabSettings';
import {TabTopBar} from '../../tabs/components/TabTopBar';
import {TabsContainerProps, tabType} from '../../tabs/models/TabsModel';
import {changeTab, changeTabOption} from '../../tabs/tabsActions';
import {gateways} from '../../validation/models/normalizedValidationData';
import {CollectionList} from '../components/CollectionList';

const CollectionTabsContainer = (props: TabsContainerProps) => {
  const {tabs, selectedTab, changeTab, changeTabOption} = props;
  const onChangeTab = (tab: tabType) => {
    changeTab({
      useCase: 'collection',
      tab,
    });
  };
  const onChangeTabOption = (tab: tabType, option: string): void => {
    changeTabOption({
      useCase: 'collection',
      tab,
      option,
    });
  };

  return (
    <Tabs>
      <TabTopBar>
        <TabHeaders selectedTab={selectedTab} onChangeTab={onChangeTab}>
          <Tab title={translate('map')} tab={tabType.map}/>
          <Tab title={translate('list')} tab={tabType.list}/>
        </TabHeaders>
        <TabOptions tab={tabType.map} selectedTab={selectedTab} select={onChangeTabOption} tabs={tabs}>
          <TabOption
            title={translate('area')}
            id={'area'}
          />
          <TabOption
            title={translate('object')}
            id={'object'}
          />
          <TabOption
            title={translate('facility')}
            id={'facility'}
          />
        </TabOptions>
        <TabSettings useCase="collection"/>
      </TabTopBar>
      <TabContent tab={tabType.map} selectedTab={selectedTab}>
        <Image src="usecases/validation/img/map.png"/>
      </TabContent>
      <TabContent tab={tabType.list} selectedTab={selectedTab}>
        <CollectionList data={gateways}/>
      </TabContent>
    </Tabs>
  );
};

const mapStateToProps = (state: RootState) => {
  const {tabs: {collection: {tabs, selectedTab}}} = state;
  return {
    selectedTab,
    tabs,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  changeTab,
  changeTabOption,
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(CollectionTabsContainer);

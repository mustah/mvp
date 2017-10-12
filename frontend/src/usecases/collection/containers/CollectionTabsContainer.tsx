import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/index';
import {translate} from '../../../services/translationService';
import {Image} from '../../common/components/images/Image';
import {Tab} from '../../common/components/tabs/components/Tab';
import {TabContent} from '../../common/components/tabs/components/TabContent';
import {TabHeaders} from '../../common/components/tabs/components/TabHeaders';
import {Tabs} from '../../common/components/tabs/components/Tabs';
import {TabSettings} from '../../common/components/tabs/components/TabSettings';
import {TabTopBar} from '../../common/components/tabs/components/TabTopBar';
import {TabsContainerProps, tabType} from '../../common/components/tabs/models/TabsModel';
import {changeTab, changeTabOption} from '../../ui/tabsActions';
import {CollectionList} from '../components/CollectionList';

interface CollectionTabsContainer extends TabsContainerProps {
  normalizedGateways: {allIds: any; byId: any};
}

const CollectionTabsContainer = (props: CollectionTabsContainer) => {
  const {selectedTab, changeTab, normalizedGateways} = props;
  const onChangeTab = (tab: tabType) => {
    changeTab({
      useCase: 'collection',
      tab,
    });
  };

  return (
    <Tabs>
      <TabTopBar>
        <TabHeaders selectedTab={selectedTab} onChangeTab={onChangeTab}>
          <Tab title={translate('list')} tab={tabType.list}/>
        </TabHeaders>
        <TabSettings useCase="collection"/>
      </TabTopBar>
      <TabContent tab={tabType.map} selectedTab={selectedTab}>
        <Image src="usecases/validation/img/map.png"/>
      </TabContent>
      <TabContent tab={tabType.list} selectedTab={selectedTab}>
        <CollectionList data={normalizedGateways}/>
      </TabContent>
    </Tabs>
  );
};

const mapStateToProps = (state: RootState) => {
  const {ui: {tabs: {collection: {tabs, selectedTab}}}} = state;
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

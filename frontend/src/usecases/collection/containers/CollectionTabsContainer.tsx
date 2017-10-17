import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/index';
import {translate} from '../../../services/translationService';
import {Tab} from '../../common/components/tabs/components/Tab';
import {TabContent} from '../../common/components/tabs/components/TabContent';
import {TabHeaders} from '../../common/components/tabs/components/TabHeaders';
import {Tabs} from '../../common/components/tabs/components/Tabs';
import {TabSettings} from '../../common/components/tabs/components/TabSettings';
import {TabTopBar} from '../../common/components/tabs/components/TabTopBar';
import {TabsContainerProps, tabType} from '../../common/components/tabs/models/TabsModel';
import {changeTab, changeTabOption} from '../../ui/tabsActions';
import {CollectionList} from '../components/CollectionList';
import {Gateway, Pagination} from '../models/Collections';
import {PaginationControl} from '../../common/components/pagination-control/PaginationControl';
import {collectionChangePage} from '../collectionActions';

interface CollectionTabsContainer extends TabsContainerProps {
  gateways: Gateway;
  pagination: Pagination;
  collectionChangePage: (page) => any;
}

const CollectionTabsContainer = (props: CollectionTabsContainer) => {
  const {selectedTab, changeTab, gateways, pagination, collectionChangePage} = props;
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
      <TabContent tab={tabType.list} selectedTab={selectedTab}>
        <CollectionList data={gateways}/>
        <PaginationControl pagination={pagination} changePage={collectionChangePage}/>
      </TabContent>
    </Tabs>
  );
};

const mapStateToProps = (state: RootState) => {
  const {ui: {tabs: {collection: {tabs, selectedTab}}}, collection} = state;
  return {
    selectedTab,
    tabs,
    gateways: collection.gateways,
    pagination: collection.pagination,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  changeTab,
  changeTabOption,
  collectionChangePage,
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(CollectionTabsContainer);

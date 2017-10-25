import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {Image} from '../../common/components/images/Image';
import {Tab} from '../../common/components/tabs/components/Tab';
import {TabContent} from '../../common/components/tabs/components/TabContent';
import {TabHeaders} from '../../common/components/tabs/components/TabHeaders';
import {TabOption} from '../../common/components/tabs/components/TabOption';
import {TabOptions} from '../../common/components/tabs/components/TabOptions';
import {Tabs} from '../../common/components/tabs/components/Tabs';
import {TabSettings} from '../../common/components/tabs/components/TabSettings';
import {TabTopBar} from '../../common/components/tabs/components/TabTopBar';
import {TabsContainerProps, tabType} from '../../common/components/tabs/models/TabsModel';
import {changeTab, changeTabOption} from '../../../state/ui/tabsActions';
import {ValidationList} from '../components/ValidationList';
import {normalizedValidationData} from '../models/normalizedValidationData';

const ValidationTabsContainer = (props: TabsContainerProps) => {
  const {tabs, selectedTab, changeTab, changeTabOption} = props;
  const onChangeTab = (tab: tabType) => {
    changeTab({
      useCase: 'validation',
      tab,
    });
  };
  const onChangeTabOption = (tab: tabType, option: string): void => {
    changeTabOption({
      useCase: 'validation',
      tab,
      option,
    });
  };

  return (
    <Tabs>
      <TabTopBar>
        <TabHeaders selectedTab={selectedTab} onChangeTab={onChangeTab}>
          <Tab title={translate('map')} tab={tabType.map} />
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
        <TabSettings useCase="validation"/>
      </TabTopBar>
      <TabContent tab={tabType.map} selectedTab={selectedTab}>
        <Image src="usecases/validation/img/map.png"/>
      </TabContent>
      <TabContent tab={tabType.list} selectedTab={selectedTab}>
        <ValidationList data={normalizedValidationData.meteringPoints}/>
      </TabContent>
    </Tabs>
  );
};

const mapStateToProps = (state: RootState) => {
  const {ui: {tabs: {validation: {tabs, selectedTab}}}} = state;
  return {
    selectedTab,
    tabs,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  changeTab,
  changeTabOption,
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(ValidationTabsContainer);

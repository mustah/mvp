import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/index';
import {translate} from '../../../services/translationService';
import {Image} from '../../common/components/images/Image';
import {TabItem} from '../../tabs/components/TabItem';
import {TabOption} from '../../tabs/components/TabOption';
import {Tabs} from '../../tabs/components/Tabs';
import {Tab, tabTypes} from '../../tabs/models/TabsModel';
import {changeTab, changeTabOption} from '../../tabs/tabsActions';

interface ValidationTabsContainerProps {
  tabs: Tab;
  selectedTab: string;
  changeTab: (payload: {useCase: string; tab: string; }) => any;
  changeTabOption: (payload: {useCase: string; tab: string; option: string; }) => any;
}

const ValidationTabsContainer = (props: ValidationTabsContainerProps) => {
  const {tabs, selectedTab, changeTab, changeTabOption} = props;
  const onChangeTab = (tab: string) => {
    changeTab({
      useCase: 'validation',
      tab,
    });
  };
  const onChangeTabOption = (tab: string, option: string): void => {
    changeTabOption({
      useCase: 'validation',
      tab,
      option,
    });
  };

  return (
    <Tabs selectedTab={selectedTab}>
      <TabItem tabName={translate('map')} tab={tabTypes.map} selectedTab={selectedTab} changeTab={onChangeTab}>
        <Image src="usecases/validation/img/map.png">
          <TabOption
            tab={tabTypes.map}
            select={onChangeTabOption}
            optionName={translate('area')}
            option={'area'}
            selectedOption={tabs[tabTypes.map].selectedOption}
          />
          <TabOption
            tab={tabTypes.map}
            select={onChangeTabOption}
            optionName={translate('object')}
            option={'object'}
            selectedOption={tabs[tabTypes.map].selectedOption}
          />
          <TabOption
            tab={tabTypes.map}
            select={onChangeTabOption}
            optionName={translate('facility')}
            option={'facility'}
            selectedOption={tabs[tabTypes.map].selectedOption}
          />
        </Image>
      </TabItem>
      <TabItem tabName={translate('list')} tab={tabTypes.list} selectedTab={selectedTab} changeTab={onChangeTab}>
        <Image src="usecases/validation/img/meters.png">
          <TabOption
            tab={tabTypes.list}
            select={onChangeTabOption}
            optionName={translate('sort descending')}
            option={'sort descending'}
            selectedOption={tabs[tabTypes.list].selectedOption}
          />
          <TabOption
            tab={tabTypes.list}
            select={onChangeTabOption}
            optionName={translate('sort ascending')}
            option={'sort ascending'}
            selectedOption={tabs[tabTypes.list].selectedOption}
          />
        </Image>
      </TabItem>
    </Tabs>
  );
};

const mapStateToProps = (state: RootState) => {
  const {tabs} = state;
  return {
    selectedTab: tabs.validation.selectedTab,
    tabs: tabs.validation.tabs,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  changeTab,
  changeTabOption,
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(ValidationTabsContainer);

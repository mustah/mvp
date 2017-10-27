import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {changeTab, changeTabOption} from '../../../state/ui/tabs/tabsActions';
import {uuid} from '../../../types/Types';
import {PieChartSelector, PieClick} from '../../common/components/pie-chart-selector/PieChartSelector';
import {Tab} from '../../common/components/tabs/components/Tab';
import {TabContent} from '../../common/components/tabs/components/TabContent';
import {TabHeaders} from '../../common/components/tabs/components/TabHeaders';
import {TabOption} from '../../common/components/tabs/components/TabOption';
import {TabOptions} from '../../common/components/tabs/components/TabOptions';
import {Tabs} from '../../common/components/tabs/components/Tabs';
import {TabSettings} from '../../common/components/tabs/components/TabSettings';
import {TabTopBar} from '../../common/components/tabs/components/TabTopBar';
import {TabsContainerProps, tabType} from '../../common/components/tabs/models/TabsModel';
import MapContainer from '../../map/containers/MapContainer';
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

  const cities = [
    {name: 'Älmhult', value: 822},
    {name: 'Perstorp', value: 893},
  ];

  // TODO the city is maybe not an uuid here, but a string.. yikes
  const selectCity: PieClick = (city: uuid) => alert('You selected the city ' + city);

  const productModels = [
    {name: 'CMe2100', value: 66},
    {name: 'CMi2110', value: 1649},
  ];

  // TODO the city is maybe not an uuid here, but a string.. yikes
  const selectProductModel: PieClick =
    (productModel: uuid) => alert('You selected the product model ' + productModel);

  /**
   * We want the pie charts to differentiate against each other
   * We can use a service like https://www.sessions.edu/color-calculator/
   * to find sets of "splít complimentary", "triadic" or "tetriadic" colors.
   */
  const colors: [string[]] = [
    ['#E8A090', '#FCE8CC'],
    ['#588E95', '#CCD9CE'],
  ];

  return (
    <Tabs>
      <TabTopBar>
        <TabHeaders selectedTab={selectedTab} onChangeTab={onChangeTab}>
          <Tab title={translate('dashboard')} tab={tabType.dashboard}/>
          <Tab title={translate('list')} tab={tabType.list}/>
          <Tab title={translate('map')} tab={tabType.map}/>
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
      <TabContent tab={tabType.dashboard} selectedTab={selectedTab}>
        <PieChartSelector onClick={selectCity} data={cities} colors={colors[0]}/>
        <PieChartSelector onClick={selectProductModel} data={productModels} colors={colors[1]}/>
      </TabContent>
      <TabContent tab={tabType.map} selectedTab={selectedTab}>
        <MapContainer/>
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

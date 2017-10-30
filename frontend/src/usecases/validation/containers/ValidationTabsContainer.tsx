import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {Meter} from '../../../state/domain-models/meter/meterModels';
import {getMeterEntities, getMetersTotal} from '../../../state/domain-models/meter/meterSelectors';
import {changeTabOptionValidation, changeTabValidation} from '../../../state/ui/tabs/tabsActions';
import {getSelectedTab, getTabs} from '../../../state/ui/tabs/tabsSelectors';
import {uuid} from '../../../types/Types';
import {Row} from '../../common/components/layouts/row/Row';
import {PaginationControl} from '../../common/components/pagination-control/PaginationControl';
import {PieChartSelector, PieClick} from '../../common/components/pie-chart-selector/PieChartSelector';
import {MeterList} from '../../common/components/table/MeterList';
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
import {changePaginationValidation} from '../../ui/pagination/paginationActions';
import {Pagination} from '../../ui/pagination/paginationModels';
import {getPaginationList, getValidationPagination} from '../../ui/pagination/paginationSelectors';

interface ValidationTabsContainerProps extends TabsContainerProps {
  numOfMeters: number;
  meters: { [key: string]: Meter };
  paginatedList: uuid[];
  pagination: Pagination;
  paginationChangePage: (page: number) => any;
}

const ValidationTabsContainer = (props: ValidationTabsContainerProps) => {
  const {
    tabs, changeTabOption, selectedTab, changeTab,
    meters, pagination, paginationChangePage, paginatedList, numOfMeters,
  } = props;

  const cities = [
    {name: 'Älmhult', value: 822},
    {name: 'Perstorp', value: 893},
  ];

  const selectCity: PieClick = (city: uuid) => alert('You selected the city ' + city);

  const productModels = [
    {name: 'CMe2100', value: 66},
    {name: 'CMi2110', value: 1649},
  ];

  const selectProductModel: PieClick =
    (productModel: uuid) => alert('You selected the product model ' + productModel);

  const statuses = [
    {name: translate('ok'), value: 1713},
    {name: translate('reported'), value: 2},
    {name: translate('could not be collected'), value: 0},
  ];

  const selectStatus: PieClick =
    (status: uuid) => alert('You selected the status ' + status);

  const colors: [string[]] = [
    ['#56b9d0', '#344d6c'],
    ['#fbba42', '#3b3f42'],
    ['#b7e000', '#f7be29', '#ed4200'],
  ];

  return (
    <Tabs>
      <TabTopBar>
        <TabHeaders selectedTab={selectedTab} onChangeTab={changeTab}>
          <Tab title={translate('dashboard')} tab={tabType.dashboard}/>
          <Tab title={translate('list')} tab={tabType.list}/>
          <Tab title={translate('map')} tab={tabType.map}/>
        </TabHeaders>
        <TabOptions tab={tabType.map} selectedTab={selectedTab} select={changeTabOption} tabs={tabs}>
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
        <TabSettings useCase={'validation'}/>
      </TabTopBar>
      <TabContent tab={tabType.dashboard} selectedTab={selectedTab}>
        <Row>
          <PieChartSelector onClick={selectCity} data={cities} colors={colors[0]}/>
          <PieChartSelector onClick={selectProductModel} data={productModels} colors={colors[1]}/>
          <PieChartSelector onClick={selectStatus} data={statuses} colors={colors[2]}/>
        </Row>
      </TabContent>
      <TabContent tab={tabType.map} selectedTab={selectedTab}>
        <MapContainer/>
      </TabContent>
      <TabContent tab={tabType.list} selectedTab={selectedTab}>
        <MeterList data={{allIds: paginatedList, byId: meters}}/>
        <PaginationControl pagination={pagination} numOfEntities={numOfMeters} changePage={paginationChangePage}/>
      </TabContent>
    </Tabs>
  );
};

const mapStateToProps = (state: RootState) => {
  const {ui, domainModels} = state;
  const pagination = getValidationPagination(ui);
  const meters = domainModels.meters;
  return {
    selectedTab: getSelectedTab(ui.tabs.validation),
    tabs: getTabs(ui.tabs.validation),
    numOfMeters: getMetersTotal(meters),
    meters: getMeterEntities(meters),
    paginatedList: getPaginationList({...pagination, ...meters}),
    pagination,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  changeTab: changeTabValidation,
  changeTabOption: changeTabOptionValidation,
  paginationChangePage: changePaginationValidation,
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(ValidationTabsContainer);

import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {suffix} from '../../../services/formatters';
import {translate} from '../../../services/translationService';
import {Gateway} from '../../../state/domain-models/gateway/gatewayModels';
import {getGatewayEntities, getGatewaysTotal} from '../../../state/domain-models/gateway/gatewaySelectors';
import {changePaginationCollection} from '../../../state/ui/pagination/paginationActions';
import {Pagination} from '../../../state/ui/pagination/paginationModels';
import {getCollectionPagination, getPaginationList} from '../../../state/ui/pagination/paginationSelectors';
import {changeTabCollection, changeTabOptionCollection} from '../../../state/ui/tabs/tabsActions';
import {getSelectedTab, getTabs} from '../../../state/ui/tabs/tabsSelectors';
import {uuid} from '../../../types/Types';
import {Row} from '../../common/components/layouts/row/Row';
import {PaginationControl} from '../../common/components/pagination-control/PaginationControl';
import {PieChartSelector} from '../../common/components/pie-chart-selector/PieChartSelector';
import {Tab} from '../../common/components/tabs/components/Tab';
import {TabContent} from '../../common/components/tabs/components/TabContent';
import {TabHeaders} from '../../common/components/tabs/components/TabHeaders';
import {TabOption} from '../../common/components/tabs/components/TabOption';
import {TabOptions} from '../../common/components/tabs/components/TabOptions';
import {Tabs} from '../../common/components/tabs/components/Tabs';
import {TabSettings} from '../../common/components/tabs/components/TabSettings';
import {TabTopBar} from '../../common/components/tabs/components/TabTopBar';
import {TabsContainerProps, tabType} from '../../common/components/tabs/models/TabsModel';
import MapContainer, {PopupMode} from '../../map/containers/MapContainer';
import {GatewayList} from '../components/GatewayList';
import {useCases} from '../../../types/constants';

interface CollectionTabsContainer extends TabsContainerProps {
  numOfGateways: number;
  gateways: { [key: string]: Gateway };
  paginatedList: uuid[];
  pagination: Pagination;
  paginationChangePage: (page: number) => any;
}

const CollectionTabsContainer = (props: CollectionTabsContainer) => {
  const {
    selectedTab,
    changeTab,
    gateways,
    pagination,
    paginationChangePage,
    paginatedList,
    numOfGateways,
    changeTabOption,
    tabs,
  } = props;

  const cities = {
    all: [
      {name: 'Älmhult', value: 822},
      {name: 'Perstorp', value: 893},
    ],
    ok: [
      {name: 'Älmhult', value: 821},
      {name: 'Perstorp', value: 892},
    ],
    warnings: [
      {name: 'Älmhult', value: 0},
      {name: 'Perstorp', value: 0},
    ],
    faults: [
      {name: 'Älmhult', value: 1},
      {name: 'Perstorp', value: 1},
    ],
  };

  const productModels = {
    all: [
      {name: 'CMe2100', value: 66},
      {name: 'CMi2110', value: 1649},
    ],
    ok: [
      {name: 'CMe2100', value: 66},
      {name: 'CMi2110', value: 1647},
    ],
    warnings: [
      {name: 'CMe2100', value: 0},
      {name: 'CMi2110', value: 0},
    ],
    faults: [
      {name: 'CMe2100', value: 0},
      {name: 'CMi2110', value: 2},
    ],
  };

  const flagged = {
    all: [
      {name: 'Ja', value: 2},
      {name: 'Nej', value: 1713},
    ],
    ok: [
      {name: 'Ja', value: 0},
      {name: 'Nej', value: 1713},
    ],
    warnings: [
      {name: 'Ja', value: 0},
      {name: 'Nej', value: 0},
    ],
    faults: [
      {name: 'Ja', value: 2},
      {name: 'Nej', value: 0},
    ],
  };

  const colors: [string[]] = [
    ['#e8a090', '#fce8cc'],
    ['#588e95', '#ccd9ce'],
    ['#b7e000', '#f7be29', '#ed4200'],
  ];

  const numberOf = (status: string): number =>
    flagged[status].reduce((sum, tuple) => sum + tuple.value, 0);

  const headings = {
    all: [
      'Inga gateways',
      'Visar alla gateways',
    ],
    ok: [
      'Inga gateways som är OK',
      'Visar alla gateways som är OK',
    ],
    warnings: [
      'Inga gateways med varningar',
      'Visar alla gateways med varningar',
    ],
    faults: [
      'Inga gateways med fel',
      'Visar alla gateways med fel',
    ],
  };

  const graphTabContents = ((tabName: string): any => {
    const count = numberOf(tabName);
    const byCity = cities[tabName];
    const byProduct = productModels[tabName];
    const header = count > 0 ? `${headings[tabName][1]}: ${count}` : headings[tabName][0];

    return count > 0 ? (
      <div className="GraphContainer">
        <h2>{header}</h2>
        <Row>
          <PieChartSelector heading="Flaggade för åtgärd" data={flagged.all} colors={colors[1]}/>
          <PieChartSelector heading="Städer" data={byCity} colors={colors[0]}/>
          <PieChartSelector heading="Produktmodeller" data={byProduct} colors={colors[1]}/>
        </Row>
      </div>
    ) : (
      <div className="GraphContainer">
        <h2>{header}</h2>
      </div>
    );
  })(tabs.graph.selectedOption);

  const graphTabs: any[] = [
    {
      id: 'all',
      label: 'ALLA',
    },
    {
      id: 'ok',
      label: 'OK',
    },
    {
      id: 'warnings',
      label: 'VARNINGAR',
    },
    {
      id: 'faults',
      label: 'FEL',
    },
  ].map((section) => {
    section.label = `${section.label}: ${suffix(numberOf(section.id))}`;
    return section;
  }).map((section) => {
    return <TabOption key={section.id} title={section.label} id={section.id}/>;
  });

  return (
    <Tabs>
      <TabTopBar>
        <TabHeaders selectedTab={selectedTab} onChangeTab={changeTab}>
          <Tab tab={tabType.graph} title={translate('graph')}/>
          <Tab tab={tabType.list} title={translate('list')}/>
          <Tab tab={tabType.map} title={translate('map')}/>
        </TabHeaders>
        <TabOptions tab={tabType.graph} selectedTab={selectedTab} select={changeTabOption} tabs={tabs}>
          {graphTabs}
        </TabOptions>
        <TabSettings useCase={useCases.collection}/>
      </TabTopBar>
      <TabContent tab={tabType.graph} selectedTab={selectedTab}>
        {graphTabContents}
      </TabContent>
      <TabContent tab={tabType.list} selectedTab={selectedTab}>
        <GatewayList data={{allIds: paginatedList, byId: gateways}}/>
        <PaginationControl pagination={pagination} changePage={paginationChangePage} numOfEntities={numOfGateways}/>
      </TabContent>
      <TabContent tab={tabType.map} selectedTab={selectedTab}>
        <MapContainer markers={gateways} popupMode={PopupMode.gateway}/>
      </TabContent>
    </Tabs>
  );
};

const mapStateToProps = (state: RootState) => {
  const {ui, domainModels} = state;
  const pagination = getCollectionPagination(ui);
  const gateways = domainModels.gateways;

  return {
    selectedTab: getSelectedTab(ui.tabs.collection),
    tabs: getTabs(ui.tabs.collection),
    numOfGateways: getGatewaysTotal(gateways),
    gateways: getGatewayEntities(gateways),
    paginatedList: getPaginationList({...pagination, ...gateways}),
    pagination,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  changeTab: changeTabCollection,
  changeTabOption: changeTabOptionCollection,
  paginationChangePage: changePaginationCollection,
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(CollectionTabsContainer);

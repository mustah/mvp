import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {suffix} from '../../../services/formatters';
import {translate} from '../../../services/translationService';
import {getResultDomainModels} from '../../../state/domain-models/domainModelsSelectors';
import {Gateway} from '../../../state/domain-models/gateway/gatewayModels';
import {getGatewayEntities, getGatewaysTotal} from '../../../state/domain-models/gateway/gatewaySelectors';
import {addSelection} from '../../../state/search/selection/selectionActions';
import {parameterNames, SelectionParameter} from '../../../state/search/selection/selectionModels';
import {changePaginationCollection} from '../../../state/ui/pagination/paginationActions';
import {Pagination} from '../../../state/ui/pagination/paginationModels';
import {getCollectionPagination, getPaginationList} from '../../../state/ui/pagination/paginationSelectors';
import {changeTabCollection, changeTabOptionCollection} from '../../../state/ui/tabs/tabsActions';
import {getSelectedTab, getTabs} from '../../../state/ui/tabs/tabsSelectors';
import {Children, uuid} from '../../../types/Types';
import {Row} from '../../common/components/layouts/row/Row';
import {PaginationControl} from '../../common/components/pagination-control/PaginationControl';
import {PieChartSelector, PieData} from '../../common/components/pie-chart-selector/PieChartSelector';
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

interface CollectionTabsContainer extends TabsContainerProps {
  entityCount: number;
  entities: {[key: string]: Gateway};
  paginatedList: uuid[];
  pagination: Pagination;
  paginationChangePage: (page: number) => any;
  selectedEntities: uuid[];
  addSelection: (searchParameters: SelectionParameter) => void;
}

/**
 * Examples:
 * - incProp({}, 'hello') => {hello: 1}
 * - incProp({hello: 2}, 'hello') => {hello: 3}
 *
 * @param obj
 * @param {string} prop
 */
const incProp = (obj: any, prop: string): void =>
  typeof obj[prop] === 'undefined' ? obj[prop] = 1 : obj[prop] = obj[prop] + 1;

const CollectionTabsContainer = (props: CollectionTabsContainer) => {
  const {
    selectedTab,
    changeTab,
    entities,
    pagination,
    paginationChangePage,
    paginatedList,
    selectedEntities,
    entityCount,
    changeTabOption,
    tabs,
    addSelection,
  } = props;

  // [1] from http://materialuicolors.co/ at level 600
  const colors: [string[]] = [
    ['#e8a090', '#fce8cc'],
    ['#1E88E5', '#FDD835', '#D81B60', '#00897B'],
    ['#b7e000', '#f7be29', '#ed4200'],
  ];

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

  // TODO move this into a backend, it will be too number-crunchy for the front end to handle with big numbers
  const categories = () => ({flagged: [], cities: [], productModels: []});

  // neither Object.assign({}, categories) nor {...categories} clones values, they clone references, which is a no no
  const liveData = {
    all: categories(),
    ok: categories(),
    warnings: categories(),
    faults: categories(),
  };

  // categorize the information into a format that's easy to manipulate ...
  const counts = {all: 0, ok: 0, warnings: 0, faults: 0};
  selectedEntities.forEach((id) => {
    const gateway = entities[id];
    const normalizedStatus = gateway.status.id === 0 ? 'ok' : 'faults';

    incProp(counts, 'all');

    incProp(liveData.all.cities, gateway.city.name);
    incProp(liveData.all.flagged, gateway.status.id !== 0 ? 'Ja' : 'Nej');
    incProp(liveData.all.productModels, gateway.productModel);

    incProp(counts, normalizedStatus);

    incProp(liveData[normalizedStatus].cities, gateway.city.name);
    incProp(liveData[normalizedStatus].flagged, gateway.status.id !== 0 ? 'Ja' : 'Nej');
    incProp(liveData[normalizedStatus].productModels, gateway.productModel);
  });

  // ... then normalize the current tab, for the graphs to consume
  const flagged: PieData[] = Object.entries(liveData[tabs.graph.selectedOption].flagged).map((entry) =>
    ({name: entry[0], value: entry[1]}));
  const cities: PieData[] = Object.entries(liveData[tabs.graph.selectedOption].cities).map((entry) =>
    ({name: entry[0], value: entry[1]}));
  const productModels: PieData[] = Object.entries(liveData[tabs.graph.selectedOption].productModels).map((entry) =>
    ({name: entry[0], value: entry[1]}));

  const graphTabs: any[] = [
    {id: 'all', label: 'ALLA'},
    {id: 'ok', label: 'OK'},
    {id: 'warnings', label: 'VARNINGAR'},
    {id: 'faults', label: 'FEL'},
  ].map((section) => {
    section.label = `${section.label}: ${suffix(counts[section.id])}`;
    return section;
  }).map((section) => <TabOption key={section.id} title={section.label} id={section.id}/>);

  const selectCity = (city: string) => {
    addSelection({
      parameter: parameterNames.cities,
      id: city,
      name: city,
    });
  };

  const graphTabContents = ((tabName: string): Children => {
    const count = counts[tabName];
    const header = count ? `${headings[tabName][1]}: ${count}` : headings[tabName][0];

    if (count) {
      return (
        <div className="GraphContainer">
          <h2>{header}</h2>
          <Row>
            <PieChartSelector heading="Flaggade för åtgärd" data={flagged} colors={colors[1]}/>
            <PieChartSelector heading="Städer" data={cities} colors={colors[0]} onClick={selectCity}/>
            <PieChartSelector heading="Produktmodeller" data={productModels} colors={colors[1]}/>
          </Row>
        </div>
      );
    } else {
      return (
        <div className="GraphContainer">
          <h2>{header}</h2>
        </div>
      );
    }
  })(tabs.graph.selectedOption);

  return (
    <Tabs>
      <TabTopBar>
        <TabHeaders selectedTab={selectedTab} onChangeTab={changeTab}>
          <Tab tab={tabType.graph} title="Dashboard"/>
          <Tab tab={tabType.list} title={translate('list')}/>
          <Tab tab={tabType.map} title={translate('map')}/>
        </TabHeaders>
        <TabOptions tab={tabType.graph} selectedTab={selectedTab} select={changeTabOption} tabs={tabs}>
          {graphTabs}
        </TabOptions>
        <TabSettings/>
      </TabTopBar>
      <TabContent tab={tabType.graph} selectedTab={selectedTab}>
        {graphTabContents}
      </TabContent>
      <TabContent tab={tabType.list} selectedTab={selectedTab}>
        <GatewayList data={{allIds: paginatedList, byId: entities}}/>
        <PaginationControl pagination={pagination} changePage={paginationChangePage} numOfEntities={entityCount}/>
      </TabContent>
      <TabContent tab={tabType.map} selectedTab={selectedTab}>
        <MapContainer markers={entities} popupMode={PopupMode.gateway}/>
      </TabContent>
    </Tabs>
  );
};

const mapStateToProps = (state: RootState) => {
  const {ui, domainModels} = state;
  const pagination = getCollectionPagination(ui);
  const entityState = domainModels.gateways;

  return {
    selectedTab: getSelectedTab(ui.tabs.collection),
    tabs: getTabs(ui.tabs.collection),
    entityCount: getGatewaysTotal(entityState),
    entities: getGatewayEntities(entityState),
    selectedEntities: getResultDomainModels(entityState),
    paginatedList: getPaginationList({...pagination, ...entityState}),
    pagination,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  changeTab: changeTabCollection,
  changeTabOption: changeTabOptionCollection,
  paginationChangePage: changePaginationCollection,
  addSelection,
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(CollectionTabsContainer);

import * as classNames from 'classnames';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {Column, ColumnCenter} from '../../../components/layouts/column/Column';
import {Row, RowRight} from '../../../components/layouts/row/Row';
import {WrapperIndent} from '../../../components/layouts/wrapper/Wrapper';
import {PaginationControl} from '../../../components/pagination-control/PaginationControl';
import {PieChartSelector, PieData} from '../../../components/pie-chart-selector/PieChartSelector';
import {Tab} from '../../../components/tabs/components/Tab';
import {TabContent} from '../../../components/tabs/components/TabContent';
import {TabHeaders} from '../../../components/tabs/components/TabHeaders';
import {RaisedTabOption} from '../../../components/tabs/components/TabOption';
import {TabOptions} from '../../../components/tabs/components/TabOptions';
import {Tabs} from '../../../components/tabs/components/Tabs';
import {TabSettings} from '../../../components/tabs/components/TabSettings';
import {TabTopBar} from '../../../components/tabs/components/TabTopBar';
import {RootState} from '../../../reducers/rootReducer';
import {suffix} from '../../../services/formatters';
import {translate} from '../../../services/translationService';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {getResultDomainModels} from '../../../state/domain-models/domainModelsSelectors';
import {Flag} from '../../../state/domain-models/flag/flagModels';
import {Gateway} from '../../../state/domain-models/gateway/gatewayModels';
import {getGatewayEntities, getGatewaysTotal} from '../../../state/domain-models/gateway/gatewaySelectors';
import {addSelection} from '../../../state/search/selection/selectionActions';
import {ParameterName, SelectionParameter} from '../../../state/search/selection/selectionModels';
import {changePaginationCollection} from '../../../state/ui/pagination/paginationActions';
import {OnChangePage, Pagination} from '../../../state/ui/pagination/paginationModels';
import {getCollectionPagination, getPaginationList} from '../../../state/ui/pagination/paginationSelectors';
import {changeTabCollection, changeTabOptionCollection} from '../../../state/ui/tabs/tabsActions';
import {TabsContainerDispatchToProps, TabsContainerStateToProps, TopLevelTab,} from '../../../state/ui/tabs/tabsModels';
import {getSelectedTab, getTabs} from '../../../state/ui/tabs/tabsSelectors';
import {Children, OnClickWithId, uuid} from '../../../types/Types';
import MapContainer, {PopupMode} from '../../map/containers/MapContainer';
import {selectEntryAdd} from '../../report/reportActions';
import {GatewayList} from '../components/GatewayList';
import './CollectionTabsContainer.scss';

interface StateToProps extends TabsContainerStateToProps {
  entityCount: number;
  entities: DomainModel<Gateway>;
  paginatedList: uuid[];
  pagination: Pagination;
  selectedEntities: uuid[];
}

interface DispatchToProps extends TabsContainerDispatchToProps {
  paginationChangePage: OnChangePage;
  addSelection: (searchParameters: SelectionParameter) => void;
  selectEntryAdd: OnClickWithId;
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

const CollectionTabsContainer = (props: StateToProps & DispatchToProps) => {
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
    selectEntryAdd,
  } = props;

  // [1] from http://materialuicolors.co/ at level 600
  const colors: [string[]] = [
    ['#e8a090', '#fce8cc'],
    ['#1E88E5', '#FDD835', '#D81B60', '#00897B'],
    ['#b7e000', '#f7be29', '#ed4200'],
  ];

  const headings = {
    all: [
      translate('no gateways'),
      translate('showing all gateways'),
    ],
    ok: [
      translate('no gateways that are ok'),
      translate('showing all gateways that are ok'),
    ],
    warnings: [
      translate('no gateways that have warnings'),
      translate('showing all gateways that have warnings'),
    ],
    faults: [
      translate('no gateways that have faults'),
      translate('showing all gateways that have faults'),
    ],
  };

  // TODO move this into a backend, it will be too number-crunchy for the front end to handle with big numbers
  const categories = () => ({flagged: [], cities: [], productModels: [], status: []});

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
    incProp(liveData.all.productModels, gateway.productModel);
    incProp(liveData.all.status, gateway.status.name);

    incProp(counts, normalizedStatus);

    incProp(liveData[normalizedStatus].cities, gateway.city.name);
    if (gateway.flags.length) {
      gateway.flags.map((flag: Flag) => {
        incProp(liveData.all.flagged, flag.title);
        incProp(liveData[normalizedStatus].flagged, flag.title);
      });
    } else {
      incProp(liveData.all.flagged, translate('none'));
      incProp(liveData[normalizedStatus].flagged, translate('none'));
    }
    incProp(liveData[normalizedStatus].productModels, gateway.productModel);
    incProp(liveData[normalizedStatus].status, gateway.status.name);
  });

  const {selectedOption} = tabs.overview;

  // ... then normalize the current tab, for the graphs to consume
  const status: PieData[] = Object.entries(liveData[selectedOption].status).map((entry) =>
    ({name: entry[0], value: entry[1]}));
  const flagged: PieData[] = Object.entries(liveData[selectedOption].flagged).map((entry) =>
    ({name: entry[0], value: entry[1]}));
  const cities: PieData[] = Object.entries(liveData[selectedOption].cities).map((entry) =>
    ({name: entry[0], value: entry[1]}));
  const productModels: PieData[] = Object.entries(liveData[selectedOption].productModels).map((entry) =>
    ({name: entry[0], value: entry[1]}));

  const overviewTabOptions: any[] = [
    {id: 'all', label: translate('all')},
    {id: 'ok', label: translate('ok')},
    {id: 'warnings', label: translate('warnings')},
    {id: 'faults', label: translate('faults')},
  ].map((section) => {
    section.label = `${section.label}: ${suffix(counts[section.id])}`;
    return section;
  }).map((section) => (
    <RaisedTabOption
      className={classNames(section.id)}
      id={section.id}
      key={section.id}
      title={section.label}
    />));

  const selectStatus = (status: string) => {
    const statusId = status === 'OK' ? 0 : 3;
    addSelection({
      parameter: ParameterName.gatewayStatuses,
      id: statusId,
      name: status,
    });
  };

  const selectCity = (city: string) => {
    addSelection({
      parameter: ParameterName.cities,
      id: city,
      name: city,
    });
  };

  const selectProductModel = (productModel: string) => {
    addSelection({
      parameter: ParameterName.productModels,
      id: productModel,
      name: productModel,
    });
  };

  const overviewTabContents = ((tabName: string): Children => {
    const count = counts[tabName];
    const header = count ? `${headings[tabName][1]}: ${count}` : headings[tabName][0];

    const chartRow = count > 0 ? (
      <Row>
        <PieChartSelector
          heading={translate('status')}
          data={status}
          colors={colors[0]}
          onClick={selectStatus}
        />
        <PieChartSelector
          heading={translate('flagged for action')}
          data={flagged}
          colors={colors[1]}
        />
        <PieChartSelector
          heading={translate('cities')}
          data={cities}
          colors={colors[0]}
          onClick={selectCity}
        />
        <PieChartSelector
          heading={translate('product models')}
          data={productModels}
          colors={colors[1]}
          onClick={selectProductModel}
        />
      </Row>
    ) : null;

    return (
      <WrapperIndent>
        <Row className="StatusControl">
          <Column>
            <h2 className="first-uppercase">{header}</h2>
          </Column>
          <Column className="flex-1"/>
          <ColumnCenter className="StatusTabOptions">
            <RowRight>
              <div className="first-uppercase">{translate('filter on status') + ':'}</div>
              <TabOptions tab={TopLevelTab.overview} selectedTab={selectedTab} select={changeTabOption} tabs={tabs}>
                {overviewTabOptions}
              </TabOptions>
            </RowRight>
          </ColumnCenter>
        </Row>
        {chartRow}
      </WrapperIndent>
    );
  })(selectedOption);

  return (
    <Tabs>
      <TabTopBar>
        <TabHeaders selectedTab={selectedTab} onChangeTab={changeTab}>
          <Tab tab={TopLevelTab.overview} title={translate('overview')}/>
          <Tab tab={TopLevelTab.list} title={translate('list')}/>
          <Tab tab={TopLevelTab.map} title={translate('map')}/>
        </TabHeaders>
        <TabSettings/>
      </TabTopBar>
      <TabContent tab={TopLevelTab.overview} selectedTab={selectedTab}>
        {overviewTabContents}
      </TabContent>
      <TabContent tab={TopLevelTab.list} selectedTab={selectedTab}>
        <GatewayList result={paginatedList} entities={entities} selectEntryAdd={selectEntryAdd}/>
        <PaginationControl pagination={pagination} changePage={paginationChangePage} numOfEntities={entityCount}/>
      </TabContent>
      <TabContent tab={TopLevelTab.map} selectedTab={selectedTab}>
        <MapContainer markers={entities} popupMode={PopupMode.gateway}/>
      </TabContent>
    </Tabs>
  );
};

const mapStateToProps = ({ui, domainModels: {gateways}}: RootState): StateToProps => {
  const pagination = getCollectionPagination(ui);
  return {
    selectedTab: getSelectedTab(ui.tabs.collection),
    tabs: getTabs(ui.tabs.collection),
    entityCount: getGatewaysTotal(gateways),
    entities: getGatewayEntities(gateways),
    selectedEntities: getResultDomainModels(gateways),
    paginatedList: getPaginationList({pagination, result: getResultDomainModels(gateways)}),
    pagination,
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeTab: changeTabCollection,
  changeTabOption: changeTabOptionCollection,
  paginationChangePage: changePaginationCollection,
  addSelection,
  selectEntryAdd,
}, dispatch);

export default connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(CollectionTabsContainer);

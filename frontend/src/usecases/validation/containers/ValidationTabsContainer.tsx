import * as classNames from 'classnames';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import 'ValidationTabsContainer.scss';
import {Column, ColumnCenter} from '../../../components/layouts/column/Column';
import {Row, RowRight} from '../../../components/layouts/row/Row';
import {WrapperIndent} from '../../../components/layouts/wrapper/Wrapper';
import {MeterList} from '../../../components/metering-point/MeterList';
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
import {
  TabsContainerDispatchToProps,
  TabsContainerStateToProps,
  tabType,
} from '../../../components/tabs/models/TabsModel';
import {RootState} from '../../../reducers/rootReducer';
import {suffix} from '../../../services/formatters';
import {translate} from '../../../services/translationService';
import {getResultDomainModels} from '../../../state/domain-models/domainModelsSelectors';
import {Flag} from '../../../state/domain-models/flag/flagModels';
import {Meter} from '../../../state/domain-models/meter/meterModels';
import {getMeterEntities, getMetersTotal} from '../../../state/domain-models/meter/meterSelectors';
import {addSelection} from '../../../state/search/selection/selectionActions';
import {ParameterName, SelectionParameter} from '../../../state/search/selection/selectionModels';
import {changePaginationValidation} from '../../../state/ui/pagination/paginationActions';
import {Pagination} from '../../../state/ui/pagination/paginationModels';
import {getPaginationList, getValidationPagination} from '../../../state/ui/pagination/paginationSelectors';
import {changeTabOptionValidation, changeTabValidation} from '../../../state/ui/tabs/tabsActions';
import {getSelectedTab, getTabs} from '../../../state/ui/tabs/tabsSelectors';
import {Children, OnClickWithId, uuid} from '../../../types/Types';
import MapContainer, {PopupMode} from '../../map/containers/MapContainer';
import {selectEntryAdd} from '../../report/reportActions';

interface StateToProps extends TabsContainerStateToProps {
  entityCount: number;
  entities: {[key: string]: Meter};
  paginatedList: uuid[];
  pagination: Pagination;
  selectedEntities: uuid[];
}

interface DispatchToProps extends TabsContainerDispatchToProps {
  paginationChangePage: (page: number) => any;
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

const ValidationTabsContainer = (props: StateToProps & DispatchToProps) => {
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
    ['#E91E63', '#fce8cc', '#3F51B5', '#2196F3', '#009688'],
    ['#1E88E5', '#FDD835', '#D81B60', '#00897B'],
    ['#b7e000', '#f7be29', '#ed4200'],
  ];

  const headings = {
    all: [
      'Inga mätare',
      'Visar alla mätare',
    ],
    ok: [
      'Inga mätare som är OK',
      'Visar alla mätare som är OK',
    ],
    unknown: [
      'Inga mätare med varningar',
      'Visar alla mätare med varningar',
    ],
    alarms: [
      'Inga mätare med fel',
      'Visar alla mätare med fel',
    ],
  };

  // TODO move this into a backend, it will be too number-crunchy for the front end to handle with big numbers
  const categories = () => ({flagged: [], cities: [], manufacturers: [], media: [], status: [], alarms: []});

  // neither Object.assign({}, categories) nor {...categories} clones values, they clone references, which is a no no
  const liveData = {
    all: categories(),
    ok: categories(),
    unknown: categories(),
    alarms: categories(),
  };

  const statusLabelOf = (id: uuid) => {
    if (id === 4) {
      return 'unknown';
    } else if (id === 3) {
      return 'alarms';
    } else {
      return 'ok';
    }
  };

  // categorize the information into a format that's easy to manipulate ...
  const counts = {all: 0, ok: 0, unknown: 0, alarms: 0};
  selectedEntities.forEach((id) => {
    const meter = entities[id];
    const normalizedStatus = statusLabelOf(meter.status.id);

    incProp(counts, 'all');

    incProp(liveData.all.cities, meter.city.name);
    incProp(liveData.all.manufacturers, meter.manufacturer);
    incProp(liveData.all.media, meter.medium);
    incProp(liveData.all.status, meter.status.name);
    incProp(liveData.all.alarms, meter.alarm);

    incProp(counts, normalizedStatus);

    incProp(liveData[normalizedStatus].cities, meter.city.name);
    if (meter.flags.length) {
      meter.flags.map((flag: Flag) => {
        incProp(liveData.all.flagged, flag.title);
        incProp(liveData[normalizedStatus].flagged, flag.title);
      });
    } else {
      incProp(liveData.all.flagged, 'Ingen');
      incProp(liveData[normalizedStatus].flagged, 'Ingen');
    }
    incProp(liveData[normalizedStatus].manufacturers, meter.manufacturer);
    incProp(liveData[normalizedStatus].media, meter.medium);
    incProp(liveData[normalizedStatus].status, meter.status.name);
    incProp(liveData[normalizedStatus].alarms, meter.alarm);
  });

  const {selectedOption} = tabs.overview;
  // ... then normalize the current tab, for the graphs to consume
  const status: PieData[] = Object.entries(liveData[selectedOption].status).map((entry) =>
    ({name: entry[0], value: entry[1]}));

  const flagged: PieData[] = Object.entries(liveData[selectedOption].flagged).map((entry) =>
    ({name: entry[0], value: entry[1]}));

  const alarms: PieData[] = Object.entries(liveData[selectedOption].alarms).map((entry) =>
    ({name: entry[0], value: entry[1]}));

  const cities: PieData[] = Object.entries(liveData[selectedOption].cities).map((entry) =>
    ({name: entry[0], value: entry[1]}));

  const manufacturers: PieData[] = Object.entries(liveData[selectedOption].manufacturers).map((entry) =>
    ({name: entry[0], value: entry[1]}));

  const media: PieData[] = Object.entries(liveData[selectedOption].media).map((entry) =>
    ({name: entry[0], value: entry[1]}));

  const overviewTabOptions: any[] = [
    {id: 'all', label: 'ALLA'},
    {id: 'ok', label: 'OK'},
    {id: 'unknown', label: 'OKÄNDA'},
    {id: 'alarms', label: 'LARM'},
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
      parameter: ParameterName.meterStatuses,
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

  const selectManufacturer = (manufacturer: string) => {
    addSelection({
      parameter: ParameterName.manufacturers,
      id: manufacturer,
      name: manufacturer,
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
          heading={translate('alarm', {count: alarms.length})}
          data={alarms}
          colors={colors[0]}
        />
        <PieChartSelector
          heading={translate('cities')}
          data={cities}
          colors={colors[1]}
          onClick={selectCity}
        />
        <PieChartSelector
          heading={translate('manufacturer')}
          data={manufacturers}
          colors={colors[0]}
          onClick={selectManufacturer}
        />
        <PieChartSelector
          heading={translate('medium')}
          data={media}
          colors={colors[1]}
        />
      </Row>
    ) : null;

    return (
      <WrapperIndent>
        <Row className="StatusControl">
          <Column>
            <h2>{header}</h2>
          </Column>
          <Column className="flex-1"/>
          <ColumnCenter className="StatusTabOptions">
            <RowRight>
              <div className="first-uppercase">{translate('filter on status') + ':'}</div>
              <TabOptions tab={tabType.overview} selectedTab={selectedTab} select={changeTabOption} tabs={tabs}>
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
          <Tab tab={tabType.overview} title={translate('overview')}/>
          <Tab tab={tabType.list} title={translate('list')}/>
          <Tab tab={tabType.map} title={translate('map')}/>
        </TabHeaders>
        <TabSettings/>
      </TabTopBar>
      <TabContent tab={tabType.overview} selectedTab={selectedTab}>
        {overviewTabContents}
      </TabContent>
      <TabContent tab={tabType.list} selectedTab={selectedTab}>
        <MeterList data={{allIds: paginatedList, byId: entities}} selectEntryAdd={selectEntryAdd}/>
        <PaginationControl pagination={pagination} changePage={paginationChangePage} numOfEntities={entityCount}/>
      </TabContent>
      <TabContent tab={tabType.map} selectedTab={selectedTab}>
        <MapContainer markers={entities} popupMode={PopupMode.meterpoint}/>
      </TabContent>
    </Tabs>
  );
};

const mapStateToProps = ({ui, domainModels: {meters}}: RootState): StateToProps => {
  const pagination = getValidationPagination(ui);
  return {
    selectedTab: getSelectedTab(ui.tabs.validation),
    tabs: getTabs(ui.tabs.validation),
    entityCount: getMetersTotal(meters),
    entities: getMeterEntities(meters),
    selectedEntities: getResultDomainModels(meters),
    paginatedList: getPaginationList({pagination, result: getResultDomainModels(meters)}),
    pagination,
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeTab: changeTabValidation,
  changeTabOption: changeTabOptionValidation,
  paginationChangePage: changePaginationValidation,
  addSelection,
  selectEntryAdd,
}, dispatch);

export default connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(ValidationTabsContainer);

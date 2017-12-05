import * as classNames from 'classnames';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {Dialog} from '../../../components/dialog/Dialog';
import {MeterList} from '../../../components/meters/MeterList';
import {PaginationControl} from '../../../components/pagination-control/PaginationControl';
import {Tab} from '../../../components/tabs/components/Tab';
import {TabContent} from '../../../components/tabs/components/TabContent';
import {TabHeaders} from '../../../components/tabs/components/TabHeaders';
import {RaisedTabOption} from '../../../components/tabs/components/TabOption';
import {TabOptions} from '../../../components/tabs/components/TabOptions';
import {Tabs} from '../../../components/tabs/components/Tabs';
import {TabSettings} from '../../../components/tabs/components/TabSettings';
import {TabTopBar} from '../../../components/tabs/components/TabTopBar';
import {MeterDetailsContainer} from '../../../containers/dialogs/MeterDetailsContainer';
import {RootState} from '../../../reducers/rootReducer';
import {suffix} from '../../../services/formatters';
import {translate} from '../../../services/translationService';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {getResultDomainModels} from '../../../state/domain-models/domainModelsSelectors';
import {Meter} from '../../../state/domain-models/meter/meterModels';
import {
  getMeterEntities,
  getMetersStatusAlarm,
  getMetersStatusOk,
  getMetersStatusUnknown,
  getMetersTotal,
} from '../../../state/domain-models/meter/meterSelectors';
import {addSelection} from '../../../state/search/selection/selectionActions';
import {SelectionParameter} from '../../../state/search/selection/selectionModels';
import {changePaginationValidation} from '../../../state/ui/pagination/paginationActions';
import {OnChangePage, Pagination} from '../../../state/ui/pagination/paginationModels';
import {getPaginationList, getValidationPagination} from '../../../state/ui/pagination/paginationSelectors';
import {changeTabOptionValidation, changeTabValidation} from '../../../state/ui/tabs/tabsActions';
import {TabName, TabsContainerDispatchToProps, TabsContainerStateToProps} from '../../../state/ui/tabs/tabsModels';
import {getSelectedTab, getTabs} from '../../../state/ui/tabs/tabsSelectors';
import {OnClick, OnClickWithId, uuid} from '../../../types/Types';
import {ClusterContainer} from '../../map/containers/ClusterContainer';
import {Map} from '../../map/containers/Map';
import {closeClusterDialog} from '../../map/mapActions';
import {MapState} from '../../map/mapReducer';
import {selectEntryAdd} from '../../report/reportActions';
import {Overview} from '../components/Overview';
import {OverviewHeader} from '../components/OverviewHeader';

interface StateToProps extends TabsContainerStateToProps {
  entityCount: number;
  entities: DomainModel<Meter>;
  paginatedList: uuid[];
  pagination: Pagination;
  meters: uuid[];
  metersStatusOk: uuid[];
  metersStatusAlarm: uuid[];
  metersStatusUnknown: uuid[];
  map: MapState;
}

interface DispatchToProps extends TabsContainerDispatchToProps {
  paginationChangePage: OnChangePage;
  addSelection: (searchParameters: SelectionParameter) => void;
  selectEntryAdd: OnClickWithId;
  closeClusterDialog: OnClick;
}

const ValidationTabs = (props: StateToProps & DispatchToProps) => {

  const {
    selectedTab,
    changeTab,
    entities,
    pagination,
    paginationChangePage,
    paginatedList,
    meters,
    metersStatusOk,
    metersStatusAlarm,
    metersStatusUnknown,
    entityCount,
    changeTabOption,
    tabs,
    addSelection,
    selectEntryAdd,
    map,
    closeClusterDialog,
  } = props;

  // TODO: this should not be necessary.
  const counts = {
    all: meters.length,
    ok: metersStatusOk.length,
    alarm: metersStatusAlarm.length,
    unknown: metersStatusUnknown.length,
  };

  const {selectedOption} = tabs.overview;
  const headings = {
    all: [
      translate('no meters'),
      translate('showing all meters'),
    ],
    ok: [
      translate('no meters that are ok'),
      translate('showing all meters that are ok'),
    ],
    unknown: [
      translate('no meters that have warnings'),
      translate('showing all meters that have warnings'),
    ],
    alarm: [
      translate('no meters that have faults'),
      translate('showing all meters that have faults'),
    ],
  };

  const metersByStatus = (selectedOption: string) => {
    switch (selectedOption) {
      case 'ok':
        return metersStatusOk;
      case 'alarm':
        return metersStatusAlarm;
      case 'unknown':
        return metersStatusUnknown;
      default:
        return meters;
    }
  };

  const overviewTabOptions: any[] = [
    {id: 'all', label: translate('all')},
    {id: 'ok', label: translate('ok')},
    {id: 'unknown', label: translate('unknown')},
    {id: 'alarm', label: translate('alarms')},
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

  const count = counts[selectedOption];
  const header = count ? `${headings[selectedOption][1]}: ${count}` : headings[selectedOption][0];

  const overviewHeader = (
    <OverviewHeader header={header}>
      <TabOptions tab={TabName.overview} selectedTab={selectedTab} select={changeTabOption} tabs={tabs}>
        {overviewTabOptions}
      </TabOptions>
    </OverviewHeader>
  );

  const overview = count > 0 ? (
    <Overview
      addSelection={addSelection}
      meters={metersByStatus(selectedOption)}
      metersLookup={entities}
    />
  ) : null;

  const dialog = map.selectedMarker && map.isClusterDialogOpen ? (
    <Dialog isOpen={map.isClusterDialogOpen} close={closeClusterDialog}>
      <MeterDetailsContainer
        meter={map.selectedMarker.options.mapMarker as Meter}
      />
    </Dialog>
  ) : null;

  return (
    <Tabs>
      <TabTopBar>
        <TabHeaders selectedTab={selectedTab} onChangeTab={changeTab}>
          <Tab tab={TabName.overview} title={translate('overview')}/>
          <Tab tab={TabName.list} title={translate('list')}/>
          <Tab tab={TabName.map} title={translate('map')}/>
        </TabHeaders>
        <TabSettings/>
      </TabTopBar>
      <TabContent tab={TabName.overview} selectedTab={selectedTab} className="Wrapper-indent">
        {overviewHeader}
        {overview}
      </TabContent>
      <TabContent tab={TabName.list} selectedTab={selectedTab}>
        <MeterList result={paginatedList} entities={entities} selectEntryAdd={selectEntryAdd}/>
        <PaginationControl pagination={pagination} changePage={paginationChangePage} numOfEntities={entityCount}/>
      </TabContent>
      <TabContent tab={TabName.map} selectedTab={selectedTab}>
        <Map>
          <ClusterContainer markers={entities}/>
        </Map>
        {dialog}
      </TabContent>
    </Tabs>
  );
};

const mapStateToProps = ({ui, map, domainModels: {meters}}: RootState): StateToProps => {
  const pagination = getValidationPagination(ui);
  return {
    selectedTab: getSelectedTab(ui.tabs.validation),
    tabs: getTabs(ui.tabs.validation),
    entityCount: getMetersTotal(meters),
    entities: getMeterEntities(meters),
    meters: getResultDomainModels(meters),
    metersStatusOk: getMetersStatusOk(meters),
    metersStatusAlarm: getMetersStatusAlarm(meters),
    metersStatusUnknown: getMetersStatusUnknown(meters),
    paginatedList: getPaginationList({pagination, result: getResultDomainModels(meters)}),
    pagination,
    map,
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeTab: changeTabValidation,
  changeTabOption: changeTabOptionValidation,
  paginationChangePage: changePaginationValidation,
  addSelection,
  selectEntryAdd,
  closeClusterDialog,
}, dispatch);

export const ValidationTabsContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(ValidationTabs);

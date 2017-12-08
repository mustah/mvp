import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {Dialog} from '../../../components/dialog/Dialog';
import {MeterList} from '../../../components/meters/MeterList';
import {PaginationControl} from '../../../components/pagination-control/PaginationControl';
import {Tab} from '../../../components/tabs/components/Tab';
import {TabContent} from '../../../components/tabs/components/TabContent';
import {TabHeaders} from '../../../components/tabs/components/TabHeaders';
import {Tabs} from '../../../components/tabs/components/Tabs';
import {TabSettings} from '../../../components/tabs/components/TabSettings';
import {TabTopBar} from '../../../components/tabs/components/TabTopBar';
import {MeterDetailsContainer} from '../../../containers/dialogs/MeterDetailsContainer';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {getResultDomainModels} from '../../../state/domain-models/domainModelsSelectors';
import {Meter, MeterDataSummary} from '../../../state/domain-models/meter/meterModels';
import {getMeterDataSummary, getMeterEntities, getMetersTotal} from '../../../state/domain-models/meter/meterSelectors';
import {setSelection} from '../../../state/search/selection/selectionActions';
import {SelectionParameter} from '../../../state/search/selection/selectionModels';
import {changePaginationValidation} from '../../../state/ui/pagination/paginationActions';
import {OnChangePage, Pagination} from '../../../state/ui/pagination/paginationModels';
import {getPaginationList, getValidationPagination} from '../../../state/ui/pagination/paginationSelectors';
import {changeTabValidation} from '../../../state/ui/tabs/tabsActions';
import {TabName, TabsContainerDispatchToProps, TabsContainerStateToProps} from '../../../state/ui/tabs/tabsModels';
import {getSelectedTab} from '../../../state/ui/tabs/tabsSelectors';
import {Maybe, OnClick, OnClickWithId, uuid} from '../../../types/Types';
import {ClusterContainer} from '../../map/containers/ClusterContainer';
import {Map} from '../../map/containers/Map';
import {closeClusterDialog} from '../../map/mapActions';
import {getSelectedMeterMarker} from '../../map/mapSelectors';
import {selectEntryAdd} from '../../report/reportActions';
import {ValidationOverview} from '../components/ValidationOverview';

interface StateToProps extends TabsContainerStateToProps {
  metersCount: number;
  meterDataSummary: Maybe<MeterDataSummary>;
  metersLookup: DomainModel<Meter>;
  paginatedList: uuid[];
  pagination: Pagination;
  selectedMarker?: Maybe<Meter>;
}

interface DispatchToProps extends TabsContainerDispatchToProps {
  paginationChangePage: OnChangePage;
  selectEntryAdd: OnClickWithId;
  setSelection: (searchParameters: SelectionParameter) => void;
  closeClusterDialog: OnClick;
}

const ValidationTabs = (props: StateToProps & DispatchToProps) => {
  const {
    selectedTab,
    changeTab,
    metersLookup,
    meterDataSummary,
    pagination,
    paginationChangePage,
    paginatedList,
    metersCount,
    selectEntryAdd,
    setSelection,
    selectedMarker,
    closeClusterDialog,
  } = props;

  const dialog = selectedMarker && (
    <Dialog isOpen={true} close={closeClusterDialog}>
      <MeterDetailsContainer meter={selectedMarker}/>
    </Dialog>
  );

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
      <TabContent tab={TabName.overview} selectedTab={selectedTab}>
        <ValidationOverview meterDataSummary={meterDataSummary} setSelection={setSelection}/>
      </TabContent>
      <TabContent tab={TabName.list} selectedTab={selectedTab}>
        <MeterList result={paginatedList} entities={metersLookup} selectEntryAdd={selectEntryAdd}/>
        <PaginationControl pagination={pagination} changePage={paginationChangePage} numOfEntities={metersCount}/>
      </TabContent>
      <TabContent tab={TabName.map} selectedTab={selectedTab}>
        <Map>
          <ClusterContainer markers={metersLookup}/>
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
    meterDataSummary: getMeterDataSummary(meters),
    metersCount: getMetersTotal(meters),
    metersLookup: getMeterEntities(meters),
    paginatedList: getPaginationList({pagination, result: getResultDomainModels(meters)}),
    pagination,
    selectedMarker: getSelectedMeterMarker(map),
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeTab: changeTabValidation,
  paginationChangePage: changePaginationValidation,
  selectEntryAdd,
  setSelection,
  closeClusterDialog,
}, dispatch);

export const ValidationTabsContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(ValidationTabs);

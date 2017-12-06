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
import {Meter} from '../../../state/domain-models/meter/meterModels';
import {getMeterEntities, getMetersTotal} from '../../../state/domain-models/meter/meterSelectors';
import {changePaginationValidation} from '../../../state/ui/pagination/paginationActions';
import {OnChangePage, Pagination} from '../../../state/ui/pagination/paginationModels';
import {getPaginationList, getValidationPagination} from '../../../state/ui/pagination/paginationSelectors';
import {changeTabValidation} from '../../../state/ui/tabs/tabsActions';
import {TabName, TabsContainerDispatchToProps, TabsContainerStateToProps} from '../../../state/ui/tabs/tabsModels';
import {getSelectedTab} from '../../../state/ui/tabs/tabsSelectors';
import {OnClick, OnClickWithId, uuid} from '../../../types/Types';
import {ClusterContainer} from '../../map/containers/ClusterContainer';
import {Map} from '../../map/containers/Map';
import {closeClusterDialog} from '../../map/mapActions';
import {MapState} from '../../map/mapReducer';
import {selectEntryAdd} from '../../report/reportActions';
import {ValidationOverviewContainer} from './ValidationOverviewContainer';

interface StateToProps extends TabsContainerStateToProps {
  entityCount: number;
  entities: DomainModel<Meter>;
  paginatedList: uuid[];
  pagination: Pagination;
  map: MapState;
}

interface DispatchToProps extends TabsContainerDispatchToProps {
  paginationChangePage: OnChangePage;
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
    entityCount,
    selectEntryAdd,
    map,
    closeClusterDialog,
  } = props;

  const dialog = map.selectedMarker && map.isClusterDialogOpen && (
    <Dialog isOpen={map.isClusterDialogOpen} close={closeClusterDialog}>
      <MeterDetailsContainer meter={map.selectedMarker.options.mapMarker as Meter}/>
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
      <TabContent tab={TabName.overview} selectedTab={selectedTab} className="Wrapper-indent">
        <ValidationOverviewContainer />
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
    entityCount: getMetersTotal(meters),
    entities: getMeterEntities(meters),
    paginatedList: getPaginationList({pagination, result: getResultDomainModels(meters)}),
    pagination,
    map,
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeTab: changeTabValidation,
  paginationChangePage: changePaginationValidation,
  selectEntryAdd,
  closeClusterDialog,
}, dispatch);

export const ValidationTabsContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(ValidationTabs);

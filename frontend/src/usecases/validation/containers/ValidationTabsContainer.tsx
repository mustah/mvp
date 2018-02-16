import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {Content} from '../../../components/content/Content';
import {Dialog} from '../../../components/dialog/Dialog';
import {Tab} from '../../../components/tabs/components/Tab';
import {TabContent} from '../../../components/tabs/components/TabContent';
import {TabHeaders} from '../../../components/tabs/components/TabHeaders';
import {Tabs} from '../../../components/tabs/components/Tabs';
import {TabSettings} from '../../../components/tabs/components/TabSettings';
import {TabTopBar} from '../../../components/tabs/components/TabTopBar';
import {MeterDetailsContainer} from '../../../containers/dialogs/MeterDetailsContainer';
import {MeterListContainer} from '../../../containers/meters/MeterListContainer';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {Meter, MeterDataSummary} from '../../../state/domain-models-paginated/meter/meterModels';
import {getMeterDataSummary} from '../../../state/domain-models-paginated/meter/meterSelectors';
import {ObjectsById} from '../../../state/domain-models/domainModels';
import {getEntitiesDomainModels} from '../../../state/domain-models/domainModelsSelectors';
import {setSelection} from '../../../state/search/selection/selectionActions';
import {OnSelectParameter} from '../../../state/search/selection/selectionModels';
import {changeTabValidation} from '../../../state/ui/tabs/tabsActions';
import {TabName, TabsContainerDispatchToProps, TabsContainerStateToProps} from '../../../state/ui/tabs/tabsModels';
import {getSelectedTab} from '../../../state/ui/tabs/tabsSelectors';
import {OnClick} from '../../../types/Types';
import {ClusterContainer} from '../../map/containers/ClusterContainer';
import {isMarkersWithinThreshold} from '../../map/containers/clusterHelper';
import {Map} from '../../map/containers/Map';
import {closeClusterDialog} from '../../map/mapActions';
import {getSelectedMeterMarker} from '../../map/mapSelectors';
import {ValidationOverview} from '../components/ValidationOverview';

interface StateToProps extends TabsContainerStateToProps {
  meterDataSummary: Maybe<MeterDataSummary>;
  meters: ObjectsById<Meter>;
  selectedMarker: Maybe<Meter>;
}

interface DispatchToProps extends TabsContainerDispatchToProps {
  setSelection: OnSelectParameter;
  closeClusterDialog: OnClick;
}

type Props = StateToProps & DispatchToProps;

const ValidationTabs = (props: Props) => {
  const {
    selectedTab,
    changeTab,
    meters,
    meterDataSummary,
    setSelection,
    selectedMarker,
    closeClusterDialog,
  } = props;

  const dialog = selectedMarker.isJust() && (
    <Dialog isOpen={true} close={closeClusterDialog}>
      <MeterDetailsContainer meter={selectedMarker.get()}/>
    </Dialog>
  );

  const hasMeters: boolean = isMarkersWithinThreshold(meters);

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
        <MeterListContainer componentId={'validationMeterList'}/>
      </TabContent>
      <TabContent tab={TabName.map} selectedTab={selectedTab}>
        <div>
        <Content hasContent={hasMeters} noContentText={translate('no meters')}>
        <Map>
        <ClusterContainer markers={meters}/>
        </Map>
        </Content>
        {dialog}
        </div>
      </TabContent>
    </Tabs>
  );
};

const mapStateToProps = ({ui, map, domainModels: {metersAll}}: RootState): StateToProps => {
  return {
    selectedTab: getSelectedTab(ui.tabs.validation),
    meterDataSummary: getMeterDataSummary(metersAll),
    meters: getEntitiesDomainModels(metersAll),
    selectedMarker: getSelectedMeterMarker(map),
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeTab: changeTabValidation,
  setSelection,
  closeClusterDialog,
}, dispatch);

export const ValidationTabsContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(ValidationTabs);

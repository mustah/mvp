import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {Tab} from '../../../components/tabs/components/Tab';
import {TabContent} from '../../../components/tabs/components/TabContent';
import {TabHeaders} from '../../../components/tabs/components/TabHeaders';
import {Tabs} from '../../../components/tabs/components/Tabs';
import {TabSettings} from '../../../components/tabs/components/TabSettings';
import {TabTopBar} from '../../../components/tabs/components/TabTopBar';
import {MeterListContainer} from '../../../containers/meters/MeterListContainer';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {changeTabValidation} from '../../../state/ui/tabs/tabsActions';
import {TabName, TabsContainerDispatchToProps, TabsContainerStateToProps} from '../../../state/ui/tabs/tabsModels';
import {getSelectedTab} from '../../../state/ui/tabs/tabsSelectors';

// interface StateToProps extends TabsContainerStateToProps {
//   // meterDataSummary: Maybe<MeterDataSummary>;
//   // meters: ObjectsById<Meter>;
//   // selectedMarker: Maybe<Meter>;
// }
//
// interface DispatchToProps extends TabsContainerDispatchToProps {
//   // setSelection: OnSelectParameter;
//   // closeClusterDialog: OnClick;
// }

type Props = TabsContainerStateToProps & TabsContainerDispatchToProps;

// TODO: ValdationTabsContainer, components that don't hadle the paginated endpoints need to be fixed.
const ValidationTabs = (props: Props) => {
  const {
    selectedTab,
    changeTab,
    // meters,
    // meterDataSummary,
    // setSelection,
    // selectedMarker,
    // closeClusterDialog,
  } = props;

  // const dialog = selectedMarker.isJust() && (
  //   <Dialog isOpen={true} close={closeClusterDialog}>
  //     <MeterDetailsContainer meter={selectedMarker.get()}/>
  //   </Dialog>
  // );

  // const hasMeters: boolean = isMarkersWithinThreshold(meters);

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
        {/*<ValidationOverview meterDataSummary={meterDataSummary} setSelection={setSelection}/>*/}
      </TabContent>
      <TabContent tab={TabName.list} selectedTab={selectedTab}>
        <MeterListContainer componentId={'validationMeterList'}/>
      </TabContent>
      <TabContent tab={TabName.map} selectedTab={selectedTab}>
        {/*<div>*/}
        {/*<Content hasContent={hasMeters} noContentText={translate('no meters')}>*/}
        {/*<Map>*/}
        {/*<ClusterContainer markers={meters}/>*/}
        {/*</Map>*/}
        {/*</Content>*/}
        {/*/!*{dialog}*!/*/}
        {/*</div>*/}
      </TabContent>
    </Tabs>
  );
};

const mapStateToProps = ({ui}: RootState): TabsContainerStateToProps => {
  return {
    selectedTab: getSelectedTab(ui.tabs.validation),
    // meterDataSummary: getMeterDataSummary(meters),
    // meters: getMeterEntities({...paginatedMeters, componentId: 'validation'}),
    // selectedMarker: getSelectedMeterMarker(map),
  };
};

const mapDispatchToProps = (dispatch): TabsContainerDispatchToProps => bindActionCreators({
  changeTab: changeTabValidation,
  // setSelection,
  // closeClusterDialog,
}, dispatch);

export const ValidationTabsContainer =
  connect<TabsContainerStateToProps, TabsContainerDispatchToProps>(mapStateToProps, mapDispatchToProps)(ValidationTabs);

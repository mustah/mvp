import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {HasContent} from '../../../components/content/HasContent';
import {Dialog} from '../../../components/dialog/Dialog';
import {Tab} from '../../../components/tabs/components/Tab';
import {TabContent} from '../../../components/tabs/components/TabContent';
import {TabHeaders} from '../../../components/tabs/components/TabHeaders';
import {Tabs} from '../../../components/tabs/components/Tabs';
import {TabSettings} from '../../../components/tabs/components/TabSettings';
import {TabTopBar} from '../../../components/tabs/components/TabTopBar';
import {MissingDataTitle} from '../../../components/texts/Titles';
import {MeterDetailsContainer} from '../../../containers/dialogs/MeterDetailsContainer';
import {MeterListContainer} from '../../../containers/meters/MeterListContainer';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated, translate} from '../../../services/translationService';
import {Meter, MeterDataSummary} from '../../../state/domain-models-paginated/meter/meterModels';
import {getMeterDataSummary} from '../../../state/domain-models-paginated/meter/meterSelectors';
import {DomainModel, RestGet} from '../../../state/domain-models/domainModels';
import {fetchAllMeters} from '../../../state/domain-models/domainModelsActions';
import {getDomainModel} from '../../../state/domain-models/domainModelsSelectors';
import {setSelection} from '../../../state/search/selection/selectionActions';
import {OnSelectParameter} from '../../../state/search/selection/selectionModels';
import {getEncodedUriParametersForAllMeters} from '../../../state/search/selection/selectionSelectors';
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
  meters: DomainModel<Meter>;
  selectedMarker: Maybe<Meter>;
  encodedUriParametersForAllMeters: string;
}

interface DispatchToProps extends TabsContainerDispatchToProps {
  setSelection: OnSelectParameter;
  closeClusterDialog: OnClick;
  fetchAllMeters: RestGet;
}

type Props = StateToProps & DispatchToProps;

class ValidationTabs extends React.Component<Props> {

  componentDidMount() {
    const {fetchAllMeters, encodedUriParametersForAllMeters} = this.props;
    fetchAllMeters(encodedUriParametersForAllMeters);
  }

  componentWillReceiveProps({fetchAllMeters, encodedUriParametersForAllMeters}: Props) {
    fetchAllMeters(encodedUriParametersForAllMeters);
  }

  render() {
    const {
      selectedTab,
      changeTab,
      meters,
      meterDataSummary,
      setSelection,
      selectedMarker,
      closeClusterDialog,
    } = this.props;

    const dialog = selectedMarker.isJust() && (
      <Dialog isOpen={true} close={closeClusterDialog}>
        <MeterDetailsContainer meter={selectedMarker.get()}/>
      </Dialog>
    );

    const noMetersFallbackContent = <MissingDataTitle title={firstUpperTranslated('no meters')}/>;

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
          <HasContent hasContent={meterDataSummary.isJust()} fallbackContent={noMetersFallbackContent}>
            <ValidationOverview meterDataSummary={meterDataSummary} setSelection={setSelection}/>
          </HasContent>
        </TabContent>
        <TabContent tab={TabName.list} selectedTab={selectedTab}>
          <MeterListContainer componentId={'validationMeterList'}/>
        </TabContent>
        <TabContent tab={TabName.map} selectedTab={selectedTab}>
          <div>
            <HasContent
              hasContent={isMarkersWithinThreshold(meters.entities) && meters.result.length > 0}
              fallbackContent={noMetersFallbackContent}
            >
              <Map>
                <ClusterContainer markers={meters.entities}/>
              </Map>
            </HasContent>
            {dialog}
          </div>
        </TabContent>
      </Tabs>
    );
  }
}

const mapStateToProps = ({ui, searchParameters, map, domainModels: {allMeters}}: RootState): StateToProps => ({
  selectedTab: getSelectedTab(ui.tabs.validation),
  meterDataSummary: getMeterDataSummary(allMeters),
  meters: getDomainModel(allMeters),
  selectedMarker: getSelectedMeterMarker(map),
  encodedUriParametersForAllMeters: getEncodedUriParametersForAllMeters(searchParameters),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeTab: changeTabValidation,
  setSelection,
  closeClusterDialog,
  fetchAllMeters,
}, dispatch);

export const ValidationTabsContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(ValidationTabs);

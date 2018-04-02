import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {HasContent} from '../../../components/content/HasContent';
import {Dialog} from '../../../components/dialog/Dialog';
import {Loader} from '../../../components/loading/Loader';
import {Tab} from '../../../components/tabs/components/Tab';
import {TabContent} from '../../../components/tabs/components/TabContent';
import {TabHeaders} from '../../../components/tabs/components/TabHeaders';
import {Tabs} from '../../../components/tabs/components/Tabs';
import {TabSettings} from '../../../components/tabs/components/TabSettings';
import {TabTopBar} from '../../../components/tabs/components/TabTopBar';
import {MissingDataTitle} from '../../../components/texts/Titles';
import {GatewayDetailsContainer} from '../../../containers/dialogs/GatewayDetailsContainer';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated, translate} from '../../../services/translationService';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {getDomainModel, getError} from '../../../state/domain-models/domainModelsSelectors';
import {getGatewayParameters} from '../../../state/search/selection/selectionSelectors';
import {changePaginationPage} from '../../../state/ui/pagination/paginationActions';
import {OnChangePage} from '../../../state/ui/pagination/paginationModels';
import {changeTabCollection} from '../../../state/ui/tabs/tabsActions';
import {
  TabName,
  TabsContainerDispatchToProps,
  TabsContainerStateToProps,
} from '../../../state/ui/tabs/tabsModels';
import {getSelectedTab} from '../../../state/ui/tabs/tabsSelectors';
import {
  ClearError,
  EncodedUriParameters,
  ErrorResponse,
  Fetch,
  OnClick,
  uuid,
} from '../../../types/Types';
import {ClusterContainer} from '../../map/containers/ClusterContainer';
import {metersWithinThreshold} from '../../map/containers/clusterHelper';
import {Map} from '../../map/containers/Map';
import {
  clearErrorGatewayMapMarkers,
  fetchGatewayMapMarkers,
} from '../../map/gatewayMapMarkerApiActions';
import {closeClusterDialog} from '../../map/mapActions';
import {MapMarker} from '../../map/mapModels';
import {getSelectedMapMarker} from '../../map/mapSelectors';
import {GatewayListContainer} from '../components/GatewayListContainer';

interface StateToProps extends TabsContainerStateToProps {
  gatewayMapMarkers: DomainModel<MapMarker>;
  parameters?: EncodedUriParameters;
  selectedMarker: Maybe<uuid>;
  isFetching: boolean;
  error: Maybe<ErrorResponse>;
}

interface DispatchToProps extends TabsContainerDispatchToProps {
  changePaginationPage: OnChangePage;
  closeClusterDialog: OnClick;
  clearError: ClearError;
  fetchGatewayMapMarkers: Fetch;
}

type Props = StateToProps & DispatchToProps;

class CollectionTabs extends React.Component<Props> {

  componentDidMount() {
    const {fetchGatewayMapMarkers, parameters} = this.props;
    fetchGatewayMapMarkers(parameters);
  }

  componentWillReceiveProps({fetchGatewayMapMarkers, parameters}: Props) {
    fetchGatewayMapMarkers(parameters);
  }

  render() {
    const {
      selectedTab,
      changeTab,
      selectedMarker,
      closeClusterDialog,
      isFetching,
      error,
      gatewayMapMarkers,
      clearError,
    } = this.props;

    const hasGateways: boolean = metersWithinThreshold(gatewayMapMarkers.entities).length > 0;

    const dialog = selectedMarker.isJust() && (
      <Dialog isOpen={true} close={closeClusterDialog}>
        <GatewayDetailsContainer gatewayId={selectedMarker.get()}/>
      </Dialog>
    );

    const noGatewaysFallbackContent =
      <MissingDataTitle title={firstUpperTranslated('no gateways')}/>;

    return (
      <Tabs>
        <TabTopBar>
          <TabHeaders selectedTab={selectedTab} onChangeTab={changeTab}>
            <Tab tab={TabName.list} title={translate('list')}/>
            <Tab tab={TabName.map} title={translate('map')}/>
          </TabHeaders>
          <TabSettings/>
        </TabTopBar>
        <TabContent tab={TabName.list} selectedTab={selectedTab}>
          <GatewayListContainer componentId="collectionGatewayList"/>
        </TabContent>
        <TabContent tab={TabName.map} selectedTab={selectedTab}>
          <Loader isFetching={isFetching} error={error} clearError={clearError}>
            <div>
              <HasContent hasContent={hasGateways} fallbackContent={noGatewaysFallbackContent}>
                <Map
                  defaultZoom={7}
                >
                  <ClusterContainer markers={gatewayMapMarkers.entities}/>
                </Map>
              </HasContent>
              {dialog}
            </div>
          </Loader>
        </TabContent>
      </Tabs>
    );
  }
}

const mapStateToProps = ({
  ui: {pagination, tabs},
  map,
  domainModels: {gatewayMapMarkers},
  searchParameters,
}: RootState): StateToProps => {
  return {
    selectedTab: getSelectedTab(tabs.collection),
    gatewayMapMarkers: getDomainModel(gatewayMapMarkers),
    parameters: getGatewayParameters(searchParameters),
    selectedMarker: getSelectedMapMarker(map),
    isFetching: gatewayMapMarkers.isFetching,
    error: getError(gatewayMapMarkers),
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeTab: changeTabCollection,
  changePaginationPage,
  closeClusterDialog,
  fetchGatewayMapMarkers,
  clearError: clearErrorGatewayMapMarkers,
}, dispatch);

export const CollectionTabsContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(CollectionTabs);

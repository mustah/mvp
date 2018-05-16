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
import {TabTopBar} from '../../../components/tabs/components/TabTopBar';
import {MissingDataTitle} from '../../../components/texts/Titles';
import {GatewayDetailsContainer} from '../../../containers/dialogs/GatewayDetailsContainer';
import {now} from '../../../helpers/dateHelpers';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated, translate} from '../../../services/translationService';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {getDomainModel, getError} from '../../../state/domain-models/domainModelsSelectors';
import {changePaginationPage} from '../../../state/ui/pagination/paginationActions';
import {OnChangePage} from '../../../state/ui/pagination/paginationModels';
import {changeTabCollection} from '../../../state/ui/tabs/tabsActions';
import {
  TabName,
  TabsContainerDispatchToProps,
  TabsContainerStateToProps,
} from '../../../state/ui/tabs/tabsModels';
import {getSelectedTab} from '../../../state/ui/tabs/tabsSelectors';
import {getGatewayParameters} from '../../../state/user-selection/userSelectionSelectors';
import {
  ClearError,
  EncodedUriParameters,
  ErrorResponse,
  Fetch,
  OnClick,
  uuid,
} from '../../../types/Types';
import {Map} from '../../map/components/Map';
import {ClusterContainer} from '../../map/containers/ClusterContainer';
import {
  clearErrorGatewayMapMarkers,
  fetchGatewayMapMarkers,
} from '../../map/gatewayMapMarkerApiActions';
import {closeClusterDialog} from '../../map/mapActions';
import {Bounds, MapMarker} from '../../map/mapModels';
import {
  getBounds,
  getGatewayLowConfidenceTextInfo,
  getSelectedMapMarker,
} from '../../map/mapSelectors';
import {GatewayListContainer} from '../components/GatewayListContainer';

interface StateToProps extends TabsContainerStateToProps {
  gatewayMapMarkers: DomainModel<MapMarker>;
  parameters?: EncodedUriParameters;
  selectedMarker: Maybe<uuid>;
  isFetching: boolean;
  error: Maybe<ErrorResponse>;
  bounds?: Bounds;
  lowConfidenceText?: string;
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
      bounds,
      lowConfidenceText,
      selectedTab,
      changeTab,
      selectedMarker,
      closeClusterDialog,
      isFetching,
      error,
      gatewayMapMarkers,
      clearError,
    } = this.props;

    const dialog = selectedMarker.isJust() && (
      <Dialog isOpen={true} close={closeClusterDialog} autoScrollBodyContent={false}>
        <GatewayDetailsContainer gatewayId={selectedMarker.get()}/>
      </Dialog>
    );

    return (
      <Tabs>
        <TabTopBar>
          <TabHeaders selectedTab={selectedTab} onChangeTab={changeTab}>
            <Tab tab={TabName.list} title={translate('list')}/>
            <Tab tab={TabName.map} title={translate('map')}/>
          </TabHeaders>
        </TabTopBar>
        <TabContent tab={TabName.list} selectedTab={selectedTab}>
          <GatewayListContainer componentId="collectionGatewayList"/>
        </TabContent>
        <TabContent tab={TabName.map} selectedTab={selectedTab}>
          <Loader isFetching={isFetching} error={error} clearError={clearError}>
            <div>
              <HasContent
                hasContent={gatewayMapMarkers.result.length > 0}
                fallbackContent={<MissingDataTitle title={firstUpperTranslated('no gateways')}/>}
              >
                <Map bounds={bounds} lowConfidenceText={lowConfidenceText}>
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

const mapStateToProps =
  ({
    ui: {pagination, tabs},
    map,
    domainModels: {gatewayMapMarkers},
    userSelection: {userSelection},
  }: RootState): StateToProps =>
    ({
      bounds: getBounds(gatewayMapMarkers),
      lowConfidenceText: getGatewayLowConfidenceTextInfo(gatewayMapMarkers),
      selectedTab: getSelectedTab(tabs.collection),
      gatewayMapMarkers: getDomainModel(gatewayMapMarkers),
      parameters: getGatewayParameters({userSelection, now: now()}),
      selectedMarker: getSelectedMapMarker(map),
      isFetching: gatewayMapMarkers.isFetching,
      error: getError(gatewayMapMarkers),
    });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeTab: changeTabCollection,
  changePaginationPage,
  closeClusterDialog,
  fetchGatewayMapMarkers,
  clearError: clearErrorGatewayMapMarkers,
}, dispatch);

export const CollectionTabsContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(CollectionTabs);

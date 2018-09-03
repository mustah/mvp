import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {Dialog} from '../../../components/dialog/Dialog';
import {SearchBox} from '../../../components/dropdown-selector/SearchBox';
import {withEmptyContent, WithEmptyContentProps} from '../../../components/hoc/withEmptyContent';
import {Loader} from '../../../components/loading/Loader';
import {Tab} from '../../../components/tabs/components/Tab';
import {TabContent} from '../../../components/tabs/components/TabContent';
import {TabHeaders} from '../../../components/tabs/components/TabHeaders';
import {Tabs} from '../../../components/tabs/components/Tabs';
import {TabTopBar} from '../../../components/tabs/components/TabTopBar';
import {GatewayDetailsContainer} from '../../../containers/dialogs/GatewayDetailsContainer';
import {now} from '../../../helpers/dateHelpers';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated, translate} from '../../../services/translationService';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {getDomainModel, getError} from '../../../state/domain-models/domainModelsSelectors';
import {changePage} from '../../../state/ui/pagination/paginationActions';
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
import {closeClusterDialog} from '../../map/mapActions';
import {clearErrorGatewayMapMarkers, fetchGatewayMapMarkers} from '../../map/mapMarkerActions';
import {Bounds, MapMarker} from '../../map/mapModels';
import {
  getBounds,
  getGatewayLowConfidenceTextInfo,
  getSelectedMapMarker,
} from '../../map/mapSelectors';
import {collectionSearch} from '../../search/searchActions';
import {OnSearch, Query} from '../../search/searchModels';
import {GatewayListContainer} from './GatewayListContainer';

interface MapProps {
  bounds?: Bounds;
  lowConfidenceText?: string;
  gatewayMapMarkers: DomainModel<MapMarker>;
}

interface StateToProps extends MapProps, TabsContainerStateToProps, Query {
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
  wildcardSearch: OnSearch;
}

type Props = StateToProps & DispatchToProps;

const MapContent = ({bounds, gatewayMapMarkers, lowConfidenceText}: MapProps) => (
  <Map bounds={bounds} lowConfidenceText={lowConfidenceText}>
    <ClusterContainer markers={gatewayMapMarkers.entities}/>
  </Map>
);

const MapContentWrapper = withEmptyContent<MapProps & WithEmptyContentProps>(MapContent);

const fetchMapMarkers = ({fetchGatewayMapMarkers, parameters}: Props) =>
  fetchGatewayMapMarkers(parameters);

class CollectionTabs extends React.Component<Props> {

  componentDidMount() {
    fetchMapMarkers(this.props);
  }

  componentWillReceiveProps(props: Props) {
    fetchMapMarkers(props);
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
      query,
      wildcardSearch,
    } = this.props;

    const dialog = selectedMarker.isJust() && (
      <Dialog isOpen={true} close={closeClusterDialog} autoScrollBodyContent={false}>
        <GatewayDetailsContainer gatewayId={selectedMarker.get()}/>
      </Dialog>
    );

    const wrappedProps: MapProps & WithEmptyContentProps = {
      bounds,
      gatewayMapMarkers,
      lowConfidenceText,
      noContentText: firstUpperTranslated('no gateways'),
      hasContent: gatewayMapMarkers.result.length > 0,
    };

    return (
      <Tabs>
        <TabTopBar>
          <TabHeaders selectedTab={selectedTab} onChangeTab={changeTab}>
            <Tab tab={TabName.list} title={translate('list')}/>
            <Tab tab={TabName.map} title={translate('map')}/>
          </TabHeaders>
          <SearchBox
            onChange={wildcardSearch}
            value={query}
            className="SearchBox-list"
          />
        </TabTopBar>
        <TabContent tab={TabName.list} selectedTab={selectedTab}>
          <GatewayListContainer componentId="collectionGatewayList"/>
        </TabContent>
        <TabContent tab={TabName.map} selectedTab={selectedTab}>
          <Loader isFetching={isFetching} error={error} clearError={clearError}>
            <div>
              <MapContentWrapper {...wrappedProps}/>
              {dialog}
            </div>
          </Loader>
        </TabContent>
      </Tabs>
    );
  }
}

const mapStateToProps =
  (rootState: RootState): StateToProps => {
    const {
      ui: {tabs},
      map,
      domainModels: {gatewayMapMarkers},
      userSelection: {userSelection},
      search: {collection: {query}},
    }: RootState = rootState;
    return ({
      bounds: getBounds(gatewayMapMarkers),
      lowConfidenceText: getGatewayLowConfidenceTextInfo(rootState),
      selectedTab: getSelectedTab(tabs.collection),
      gatewayMapMarkers: getDomainModel(gatewayMapMarkers),
      parameters: getGatewayParameters({userSelection, now: now()}),
      selectedMarker: getSelectedMapMarker(map),
      isFetching: gatewayMapMarkers.isFetching,
      error: getError(gatewayMapMarkers),
      query,
    });
  };

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeTab: changeTabCollection,
  changePaginationPage: changePage,
  closeClusterDialog,
  fetchGatewayMapMarkers,
  clearError: clearErrorGatewayMapMarkers,
  wildcardSearch: collectionSearch,
}, dispatch);

export const GatewayTabsContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(CollectionTabs);
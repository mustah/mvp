import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {DispatchToProps, StateToProps} from '../../../components/tabs/components/MainContentTabs';
import {now} from '../../../helpers/dateHelpers';
import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated} from '../../../services/translationService';
import {getDomainModel, getError} from '../../../state/domain-models/domainModelsSelectors';
import {changeTabCollection} from '../../../state/ui/tabs/tabsActions';
import {getSelectedTab} from '../../../state/ui/tabs/tabsSelectors';
import {getGatewayParameters} from '../../../state/user-selection/userSelectionSelectors';
import {withMapMarkersFetcher} from '../../map/helper/mapMarkersHoc';
import {closeClusterDialog} from '../../map/mapActions';
import {clearErrorGatewayMapMarkers, fetchGatewayMapMarkers} from '../../map/mapMarkerActions';
import {getBounds, getGatewayLowConfidenceTextInfo, getSelectedMapMarker} from '../../map/mapSelectors';
import {collectionSearch} from '../../search/searchActions';
import {CollectionTabs} from '../components/CollectionTabs';

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
      noContentText: firstUpperTranslated('no gateways'),
      selectedTab: getSelectedTab(tabs.collection),
      mapMarkers: getDomainModel(gatewayMapMarkers),
      parameters: getGatewayParameters({now: now(), userSelection, query}),
      selectedId: getSelectedMapMarker(map),
      isFetching: gatewayMapMarkers.isFetching,
      error: getError(gatewayMapMarkers),
      query,
    });
  };

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeTab: changeTabCollection,
  clearError: clearErrorGatewayMapMarkers,
  close: closeClusterDialog,
  fetchMapMarkers: fetchGatewayMapMarkers,
  wildcardSearch: collectionSearch,
}, dispatch);

export const CollectionTabsContainer =
  connect<StateToProps, DispatchToProps>(
    mapStateToProps,
    mapDispatchToProps,
  )(withMapMarkersFetcher(CollectionTabs));

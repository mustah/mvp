import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {DispatchToProps, StateToProps} from '../../../components/tabs/components/MainContentTabs';
import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated} from '../../../services/translationService';
import {getDomainModel, getError} from '../../../state/domain-models/domainModelsSelectors';
import {selectResolution} from '../../../state/report/reportActions';
import {changeTabGateway} from '../../../state/ui/tabs/tabsActions';
import {getSelectedTab} from '../../../state/ui/tabs/tabsSelectors';
import {getGatewayParameters} from '../../../state/user-selection/userSelectionSelectors';
import {withMapMarkersFetcher} from '../../map/helper/mapMarkersEnhancer';
import {closeClusterDialog} from '../../map/mapActions';
import {clearErrorGatewayMapMarkers, fetchGatewayMapMarkers} from '../../map/mapMarkerActions';
import {getBounds, getGatewayLowConfidenceTextInfo, getSelectedMapMarker} from '../../map/mapSelectors';
import {GatewayTabs} from '../components/GatewayTabs';

const mapStateToProps =
  (rootState: RootState): StateToProps => {
    const {
      ui: {tabs},
      map,
      domainModels: {gatewayMapMarkers},
      userSelection: {userSelection},
    }: RootState = rootState;
    return ({
      bounds: getBounds(gatewayMapMarkers),
      lowConfidenceText: getGatewayLowConfidenceTextInfo(rootState),
      noContentText: firstUpperTranslated('no gateways'),
      selectedTab: getSelectedTab(tabs.collection),
      mapMarkers: getDomainModel(gatewayMapMarkers),
      parameters: getGatewayParameters({userSelection}),
      selectedId: getSelectedMapMarker(map),
      isFetching: gatewayMapMarkers.isFetching,
      error: getError(gatewayMapMarkers),
      key: `gatewayTabs-${userSelection.id.toString()}`
    });
  };

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeTab: changeTabGateway,
  clearError: clearErrorGatewayMapMarkers,
  close: closeClusterDialog,
  fetchMapMarkers: fetchGatewayMapMarkers,
  selectResolution,
}, dispatch);

export const GatewayTabsContainer =
  connect<StateToProps, DispatchToProps>(
    mapStateToProps,
    mapDispatchToProps,
  )(withMapMarkersFetcher(GatewayTabs));

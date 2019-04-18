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
import {onCenterMap} from '../../map/mapActions';
import {clearErrorGatewayMapMarkers, fetchGatewayMapMarkers} from '../../map/mapMarkerActions';
import {getBounds, getGatewayLowConfidenceTextInfo, getMapZoomSettings} from '../../map/mapSelectors';
import {GatewayTabs} from '../components/GatewayTabs';

const mapStateToProps =
  (rootState: RootState): StateToProps => {
    const {
      domainModels: {gatewayMapMarkers},
      map,
      ui: {tabs},
      userSelection: {userSelection},
    }: RootState = rootState;
    return ({
      bounds: getBounds(gatewayMapMarkers),
      error: getError(gatewayMapMarkers),
      id: userSelection.id,
      isFetching: gatewayMapMarkers.isFetching,
      key: `gatewayTabs-${userSelection.id}`,
      lowConfidenceText: getGatewayLowConfidenceTextInfo(rootState),
      mapMarkers: getDomainModel(gatewayMapMarkers),
      noContentText: firstUpperTranslated('no gateways'),
      parameters: getGatewayParameters({userSelection}),
      selectedTab: getSelectedTab(tabs.collection),
      ...getMapZoomSettings(userSelection.id)(map),
    });
  };

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeTab: changeTabGateway,
  clearError: clearErrorGatewayMapMarkers,
  fetchMapMarkers: fetchGatewayMapMarkers,
  onCenterMap,
  selectResolution,
}, dispatch);

export const GatewayTabsContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(GatewayTabs);

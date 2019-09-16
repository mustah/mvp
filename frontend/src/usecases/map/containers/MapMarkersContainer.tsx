import {connect} from 'react-redux';
import {bindActionCreators, Dispatch} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated} from '../../../services/translationService';
import {getError} from '../../../state/domain-models/domainModelsSelectors';
import {getSelectedTab} from '../../../state/ui/tabs/tabsSelectors';
import {getMeterParameters} from '../../../state/user-selection/userSelectionSelectors';
import {DispatchToProps, MapMarkers, StateToProps} from '../components/MapMarkers';
import {onCenterMap} from '../mapActions';
import {clearErrorMeterMapMarkers, fetchMeterMapMarkers} from '../mapMarkerActions';
import {getBounds, getMapMarkers, getMapZoomSettings, getMeterLowConfidenceTextInfo} from '../mapSelectors';

const mapStateToProps =
  (rootState: RootState): StateToProps => {
    const {
      domainModels: {meterMapMarkers},
      map,
      search: {validation: {query}},
      ui,
      userSelection: {userSelection}
    }: RootState = rootState;
    return ({
      bounds: getBounds(meterMapMarkers),
      error: getError(meterMapMarkers),
      id: userSelection.id,
      isFetching: meterMapMarkers.isFetching,
      lowConfidenceText: getMeterLowConfidenceTextInfo(rootState),
      mapMarkerClusters: getMapMarkers(meterMapMarkers),
      noContentText: firstUpperTranslated('no meters'),
      parameters: getMeterParameters({userSelection, query}),
      selectedTab: getSelectedTab(ui),
      ...getMapZoomSettings(userSelection.id)(map),
    });
  };

const mapDispatchToProps = (dispatch: Dispatch): DispatchToProps => bindActionCreators({
  clearError: clearErrorMeterMapMarkers,
  fetchMapMarkers: fetchMeterMapMarkers,
  onCenterMap,
}, dispatch);

export const MapMarkersContainer = connect(mapStateToProps, mapDispatchToProps)(MapMarkers);

import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {DispatchToProps, StateToProps} from '../../../components/tabs/components/MainContentTabs';
import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated} from '../../../services/translationService';
import {getDomainModel, getError} from '../../../state/domain-models/domainModelsSelectors';
import {changeTabMeter} from '../../../state/ui/tabs/tabsActions';
import {getSelectedTab} from '../../../state/ui/tabs/tabsSelectors';
import {getMeterParameters} from '../../../state/user-selection/userSelectionSelectors';
import {onCenterMap} from '../../map/mapActions';
import {clearErrorMeterMapMarkers, fetchMeterMapMarkers} from '../../map/mapMarkerActions';
import {getBounds, getMapZoomSettings, getMeterLowConfidenceTextInfo} from '../../map/mapSelectors';
import {MeterTabs} from '../components/MeterTabs';

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
      key: `meterTabs-${userSelection.id}`,
      lowConfidenceText: getMeterLowConfidenceTextInfo(rootState),
      mapMarkers: getDomainModel(meterMapMarkers),
      noContentText: firstUpperTranslated('no meters'),
      parameters: getMeterParameters({userSelection, query}),
      selectedTab: getSelectedTab(ui.tabs.validation),
      ...getMapZoomSettings(userSelection.id)(map),
    });
  };

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeTab: changeTabMeter,
  clearError: clearErrorMeterMapMarkers,
  fetchMapMarkers: fetchMeterMapMarkers,
  onCenterMap,
}, dispatch);

export const MeterTabsContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(MeterTabs);

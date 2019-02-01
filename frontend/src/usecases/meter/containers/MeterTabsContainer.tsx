import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {DispatchToProps, StateToProps} from '../../../components/tabs/components/MainContentTabs';
import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated} from '../../../services/translationService';
import {getDomainModel, getError} from '../../../state/domain-models/domainModelsSelectors';
import {changeTabMeter} from '../../../state/ui/tabs/tabsActions';
import {getSelectedTab} from '../../../state/ui/tabs/tabsSelectors';
import {getMeterParameters} from '../../../state/user-selection/userSelectionSelectors';
import {withMapMarkersFetcher} from '../../map/helper/mapMarkersEnhancer';
import {closeClusterDialog} from '../../map/mapActions';
import {clearErrorMeterMapMarkers, fetchMeterMapMarkers} from '../../map/mapMarkerActions';
import {getBounds, getMeterLowConfidenceTextInfo, getSelectedMapMarker} from '../../map/mapSelectors';
import {MeterTabs} from '../components/MeterTabs';

const mapStateToProps =
  (rootState: RootState): StateToProps => {
    const {
      ui,
      userSelection: {userSelection},
      map,
      domainModels: {meterMapMarkers},
      search: {validation: {query}}
    }: RootState = rootState;
    return ({
      bounds: getBounds(meterMapMarkers),
      lowConfidenceText: getMeterLowConfidenceTextInfo(rootState),
      selectedTab: getSelectedTab(ui.tabs.validation),
      mapMarkers: getDomainModel(meterMapMarkers),
      noContentText: firstUpperTranslated('no meters'),
      selectedId: getSelectedMapMarker(map),
      parameters: getMeterParameters({userSelection, query}),
      error: getError(meterMapMarkers),
      isFetching: meterMapMarkers.isFetching,
    });
  };

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeTab: changeTabMeter,
  close: closeClusterDialog,
  clearError: clearErrorMeterMapMarkers,
  fetchMapMarkers: fetchMeterMapMarkers,
}, dispatch);

export const MeterTabsContainer =
  connect<StateToProps, DispatchToProps>(
    mapStateToProps,
    mapDispatchToProps,
  )(withMapMarkersFetcher(MeterTabs));

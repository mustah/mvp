import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {DispatchToProps, StateToProps} from '../../../components/tabs/components/MainContentTabs';
import {now} from '../../../helpers/dateHelpers';
import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated} from '../../../services/translationService';
import {getDomainModel, getError} from '../../../state/domain-models/domainModelsSelectors';
import {changeTabValidation} from '../../../state/ui/tabs/tabsActions';
import {getSelectedTab} from '../../../state/ui/tabs/tabsSelectors';
import {getMeterParameters} from '../../../state/user-selection/userSelectionSelectors';
import {withMapMarkersFetcher} from '../../map/helper/mapMarkersHoc';
import {closeClusterDialog} from '../../map/mapActions';
import {clearErrorMeterMapMarkers, fetchMeterMapMarkers} from '../../map/mapMarkerActions';
import {getBounds, getMeterLowConfidenceTextInfo, getSelectedMapMarker} from '../../map/mapSelectors';
import {clearValidationSearch, validationSearch} from '../../search/searchActions';
import {ValidationTabs} from '../components/ValidationTabs';

const mapStateToProps =
  (rootState: RootState): StateToProps => {
    const {
      ui,
      userSelection: {userSelection},
      map,
      domainModels: {meterMapMarkers},
      search: {validation: {query}},
    }: RootState = rootState;
    return ({
      bounds: getBounds(meterMapMarkers),
      lowConfidenceText: getMeterLowConfidenceTextInfo(rootState),
      selectedTab: getSelectedTab(ui.tabs.validation),
      mapMarkers: getDomainModel(meterMapMarkers),
      noContentText: firstUpperTranslated('no meters'),
      selectedId: getSelectedMapMarker(map),
      parameters: getMeterParameters({now: now(), userSelection, query}),
      error: getError(meterMapMarkers),
      isFetching: meterMapMarkers.isFetching,
      query,
    });
  };

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeTab: changeTabValidation,
  close: closeClusterDialog,
  clearError: clearErrorMeterMapMarkers,
  clearSearch: clearValidationSearch,
  fetchMapMarkers: fetchMeterMapMarkers,
  search: validationSearch,
}, dispatch);

export const ValidationTabsContainer =
  connect<StateToProps, DispatchToProps>(
    mapStateToProps,
    mapDispatchToProps,
  )(withMapMarkersFetcher(ValidationTabs));

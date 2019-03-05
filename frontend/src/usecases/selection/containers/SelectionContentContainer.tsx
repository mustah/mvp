import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {setThreshold, toggleParameter} from '../../../state/user-selection/userSelectionActions';
import {SelectionListItem, SelectionParameter, ThresholdQuery} from '../../../state/user-selection/userSelectionModels';
import {
  getSelectedAddresses,
  getSelectedAlarms,
  getSelectedCities,
  getSelectedFacilities,
  getSelectedGatewaySerials,
  getSelectedMedia,
  getSelectedOrganisations,
  getSelectedReported,
  getSelectedSecondaryAddresses,
  getThreshold,
  getUserSelectionId,
} from '../../../state/user-selection/userSelectionSelectors';
import {CallbackWith, uuid} from '../../../types/Types';
import {SelectionContent} from '../components/SelectionContent';

interface StateToProps {
  addresses: SelectionListItem[];
  alarms: SelectionListItem[];
  cities: SelectionListItem[];
  facilities: SelectionListItem[];
  gatewaySerials: SelectionListItem[];
  media: SelectionListItem[];
  organisations: SelectionListItem[];
  reported: SelectionListItem[];
  secondaryAddresses: SelectionListItem[];
  threshold: ThresholdQuery | undefined;
  userSelectionId: uuid;
}

interface DispatchToProps {
  toggleParameter: CallbackWith<SelectionParameter>;
  onChangeThreshold: CallbackWith<ThresholdQuery>;
}

export type SelectionContentProps = StateToProps & DispatchToProps;

const mapStateToProps = ({userSelection}: RootState): StateToProps => ({
  alarms: getSelectedAlarms(userSelection),
  addresses: getSelectedAddresses(userSelection),
  cities: getSelectedCities(userSelection),
  facilities: getSelectedFacilities(userSelection),
  gatewaySerials: getSelectedGatewaySerials(userSelection),
  media: getSelectedMedia(userSelection),
  organisations: getSelectedOrganisations(userSelection),
  reported: getSelectedReported(userSelection),
  secondaryAddresses: getSelectedSecondaryAddresses(userSelection),
  threshold: getThreshold(userSelection),
  userSelectionId: getUserSelectionId(userSelection),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  toggleParameter,
  onChangeThreshold: setThreshold
}, dispatch);

export const SelectionContentContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(SelectionContent);

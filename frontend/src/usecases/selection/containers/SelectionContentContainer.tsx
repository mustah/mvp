import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {toggleParameter} from '../../../state/user-selection/userSelectionActions';
import {OnSelectParameter, SelectionListItem} from '../../../state/user-selection/userSelectionModels';
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
} from '../../../state/user-selection/userSelectionSelectors';
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
}

interface DispatchToProps {
  toggleParameter: OnSelectParameter;
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
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  toggleParameter,
}, dispatch);

export const SelectionContentContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(SelectionContent);

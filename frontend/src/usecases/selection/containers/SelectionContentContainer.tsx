import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {
  SearchableProps,
  renderAddressLabel,
  renderCityLabel,
  SearchableDropdownSelector,
  ListingDropdownSelector,
} from '../../../components/dropdown-selector/DropdownSelector';
import {connectedSuperAdminOnly} from '../../../components/hoc/withRoles';
import {Column} from '../../../components/layouts/column/Column';
import {Row} from '../../../components/layouts/row/Row';
import {Subtitle} from '../../../components/texts/Titles';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {Address, City} from '../../../state/domain-models/location/locationModels';
import {
  fetchAddresses,
  fetchAlarms,
  fetchCities,
  fetchFacilities,
  fetchGatewaySerials,
  fetchMedia,
  fetchOrganisationsToSelect,
  fetchReported,
  fetchSecondaryAddresses,
  mapSelectedIdToAddress,
  mapSelectedIdToCity,
} from '../../../state/domain-models/selections/selectionsApiActions';
import {toggleParameter} from '../../../state/user-selection/userSelectionActions';
import {OnSelectParameter, ParameterName, SelectionListItem} from '../../../state/user-selection/userSelectionModels';
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
import {SearchResultList} from '../components/SelectionResultList';
import './SelectionContentContainer.scss';

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

const unknownCity: City = mapSelectedIdToCity('unknown,unknown');
const unknownAddress: Address = mapSelectedIdToAddress('unknown,unknown,unknown');

const OrganisationDropDown = connectedSuperAdminOnly<SearchableProps>(SearchableDropdownSelector);

class SelectionContent extends React.Component<StateToProps & DispatchToProps> {

  render() {
    const {
      addresses,
      alarms,
      cities,
      facilities,
      gatewaySerials,
      media,
      organisations,
      reported,
      secondaryAddresses,
      toggleParameter,
    } = this.props;

    const selectCity = (item: SelectionListItem) =>
      toggleParameter({item, parameter: ParameterName.cities});
    const selectAddress = (item: SelectionListItem) =>
      toggleParameter({item, parameter: ParameterName.addresses});
    const selectReported = (item: SelectionListItem) =>
      toggleParameter({item, parameter: ParameterName.reported});
    const selectAlarm = (item: SelectionListItem) =>
      toggleParameter({item, parameter: ParameterName.alarms});
    const selectMedium = (item: SelectionListItem) =>
      toggleParameter({item, parameter: ParameterName.media});
    const selectSecondaryAddresses = (item: SelectionListItem) =>
      toggleParameter({item, parameter: ParameterName.secondaryAddresses});
    const selectFacilities = (item: SelectionListItem) =>
      toggleParameter({item, parameter: ParameterName.facilities});
    const selectOrganisations = (item: SelectionListItem) =>
      toggleParameter({item, parameter: ParameterName.organisations});
    const selectGatewaySerials = (item: SelectionListItem) =>
      toggleParameter({item, parameter: ParameterName.gatewaySerials});

    const alarmSelectionText = translate('alarm') + ': ';
    const citySelectionText = translate('city') + ': ';
    const addressSelectionText = translate('address') + ': ';
    const reportedSelectionText = translate('reported') + ': ';
    const mediumSelectionText = translate('medium') + ': ';
    const facilitySelectionText = translate('facility') + ': ';
    const organisationSelectionText = translate('organisation') + ': ';
    const secondaryAddressSelectionText = translate('secondary address') + ': ';
    const gatewaySerialSelectionText = translate('gateway serial') + ': ';

    return (
      <Column className="SelectionContentBox">
        <Subtitle>{translate('filter')}</Subtitle>

        <Row className="SelectionDropdownOptions">
          <SearchableDropdownSelector
            fetchItems={fetchFacilities}
            selectedItems={facilities}
            selectionText={facilitySelectionText}
            select={selectFacilities}
          />
          <OrganisationDropDown
            fetchItems={fetchOrganisationsToSelect}
            selectedItems={organisations}
            selectionText={organisationSelectionText}
            select={selectOrganisations}
          />
          <SearchableDropdownSelector
            fetchItems={fetchCities}
            selectedItems={cities}
            selectionText={citySelectionText}
            select={selectCity}
            renderLabel={renderCityLabel}
            rowHeight={44}
            unknownItem={unknownCity as SelectionListItem}
          />
          <SearchableDropdownSelector
            fetchItems={fetchAddresses}
            selectedItems={addresses}
            selectionText={addressSelectionText}
            select={selectAddress}
            renderLabel={renderAddressLabel}
            rowHeight={44}
            unknownItem={unknownAddress as SelectionListItem}
          />
          <ListingDropdownSelector
            fetchItems={fetchMedia}
            selectedItems={media}
            selectionText={mediumSelectionText}
            select={selectMedium}
          />
        </Row>

        <Row className="SelectionDropdownOptions">
          <ListingDropdownSelector
            fetchItems={fetchReported}
            selectedItems={reported}
            selectionText={reportedSelectionText}
            select={selectReported}
          />
          <ListingDropdownSelector
            fetchItems={fetchAlarms}
            selectedItems={alarms}
            selectionText={alarmSelectionText}
            select={selectAlarm}
          />
          <SearchableDropdownSelector
            fetchItems={fetchSecondaryAddresses}
            selectedItems={secondaryAddresses}
            selectionText={secondaryAddressSelectionText}
            select={selectSecondaryAddresses}
          />
          <SearchableDropdownSelector
            fetchItems={fetchGatewaySerials}
            selectedItems={gatewaySerials}
            selectionText={gatewaySerialSelectionText}
            select={selectGatewaySerials}
          />
        </Row>

        <SearchResultList/>
      </Column>
    );
  }
}

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

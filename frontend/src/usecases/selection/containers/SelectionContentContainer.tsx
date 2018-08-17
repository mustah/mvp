import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import 'SelectionContentContainer.scss';
import {
  DropdownSelector,
  renderAddressLabel,
  renderCityLabel,
} from '../../../components/dropdown-selector/DropdownSelector';
import {Column} from '../../../components/layouts/column/Column';
import {Row} from '../../../components/layouts/row/Row';
import {Subtitle} from '../../../components/texts/Titles';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {Address, City} from '../../../state/domain-models/location/locationModels';
import {
  fetchAddresses,
  fetchCities,
  fetchFacilities,
  fetchGatewaySerials,
  fetchGatewayStatuses,
  fetchMedia,
  fetchMeterStatuses,
  fetchSecondaryAddresses,
  mapSelectedIdToAddress,
  mapSelectedIdToCity,
} from '../../../state/domain-models/selections/selectionsApiActions';
import {toggleParameter} from '../../../state/user-selection/userSelectionActions';
import {
  OnSelectParameter,
  ParameterName,
  SelectionListItem,
} from '../../../state/user-selection/userSelectionModels';
import {
  getSelectedAddresses,
  getSelectedCities,
  getSelectedFacilities,
  getSelectedGatewaySerials,
  getSelectedGatewayStatuses,
  getSelectedMedia,
  getSelectedMeterStatuses,
  getSelectedSecondaryAddresses,
} from '../../../state/user-selection/userSelectionSelectors';
import {uuid} from '../../../types/Types';
import {SearchResultList} from '../components/SelectionResultList';

interface StateToProps {
  addresses: SelectionListItem[];
  cities: SelectionListItem[];
  facilities: SelectionListItem[];
  gatewayStatuses: SelectionListItem[];
  gatewaySerials: SelectionListItem[];
  media: SelectionListItem[];
  meterStatuses: SelectionListItem[];
  secondaryAddresses: SelectionListItem[];
  selectionId: uuid;
}

interface DispatchToProps {
  toggleParameter: OnSelectParameter;
}

const unknownCity: City = mapSelectedIdToCity('unknown,unknown');
const unknownAddress: Address = mapSelectedIdToAddress('unknown,unknown,unknown');

class SelectionContent extends React.Component<StateToProps & DispatchToProps> {

  render() {
    const {
      addresses,
      cities,
      facilities,
      gatewayStatuses,
      gatewaySerials,
      media,
      meterStatuses,
      secondaryAddresses,
      selectionId,
      toggleParameter,
    } = this.props;

    const selectCity = (item: SelectionListItem) =>
      toggleParameter({item, parameter: ParameterName.cities});
    const selectAddress = (item: SelectionListItem) =>
      toggleParameter({item, parameter: ParameterName.addresses});
    const selectMeterStatus = (item: SelectionListItem) =>
      toggleParameter({item, parameter: ParameterName.meterStatuses});
    const selectGatewayStatus = (item: SelectionListItem) =>
      toggleParameter({item, parameter: ParameterName.gatewayStatuses});
    const selectMedium = (item: SelectionListItem) =>
      toggleParameter({item, parameter: ParameterName.media});
    const selectSecondaryAddresses = (item: SelectionListItem) =>
      toggleParameter({item, parameter: ParameterName.secondaryAddresses});
    const selectFacilities = (item: SelectionListItem) =>
      toggleParameter({item, parameter: ParameterName.facilities});
    const selectGatewaySerials = (item: SelectionListItem) =>
      toggleParameter({item, parameter: ParameterName.gatewaySerials});

    const citySelectionText = translate('city') + ': ';
    const addressSelectionText = translate('address') + ': ';
    const meterStatusSelectionText = translate('meter status') + ': ';
    const gatewayStatusSelectionText = translate('gateway status') + ': ';
    const mediumSelectionText = translate('medium') + ': ';
    const facilitySelectionText = translate('facility') + ': ';
    const secondaryAddressSelectionText = translate('secondary address') + ': ';
    const gatewaySerialSelectionText = translate('gateway serial') + ': ';

    const isNotUnknown = ({name}) => name !== 'unknown';
    const selectedCities: SelectionListItem[] =
      [...cities.filter(isNotUnknown), unknownCity as SelectionListItem];
    const selectedAddresses: SelectionListItem[] =
      [...addresses.filter(isNotUnknown), unknownAddress as SelectionListItem];

    return (
      <Column className="SelectionContentBox" key={selectionId === -1 ? 1 : selectionId}>
        <Subtitle>{translate('filter')}</Subtitle>

        <Row className="SelectionDropdownOptions">
          <DropdownSelector
            fetchItems={fetchFacilities}
            selectedItems={facilities}
            selectionText={facilitySelectionText}
            select={selectFacilities}
          />
          <DropdownSelector
            fetchItems={fetchCities}
            selectedItems={selectedCities}
            selectionText={citySelectionText}
            select={selectCity}
            renderLabel={renderCityLabel}
            rowHeight={44}
          />
          <DropdownSelector
            fetchItems={fetchAddresses}
            selectedItems={selectedAddresses}
            selectionText={addressSelectionText}
            select={selectAddress}
            renderLabel={renderAddressLabel}
            rowHeight={44}
          />
          <DropdownSelector
            fetchItems={fetchMedia}
            selectedItems={media}
            selectionText={mediumSelectionText}
            select={selectMedium}
          />
          <DropdownSelector
            fetchItems={fetchMeterStatuses}
            selectedItems={meterStatuses}
            selectionText={meterStatusSelectionText}
            select={selectMeterStatus}
          />
          <DropdownSelector
            fetchItems={fetchGatewayStatuses}
            selectedItems={gatewayStatuses}
            selectionText={gatewayStatusSelectionText}
            select={selectGatewayStatus}
          />
          <DropdownSelector
            fetchItems={fetchSecondaryAddresses}
            selectedItems={secondaryAddresses}
            selectionText={secondaryAddressSelectionText}
            select={selectSecondaryAddresses}
          />
          <DropdownSelector
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
  addresses: getSelectedAddresses(userSelection),
  cities: getSelectedCities(userSelection),
  facilities: getSelectedFacilities(userSelection),
  gatewaySerials: getSelectedGatewaySerials(userSelection),
  gatewayStatuses: getSelectedGatewayStatuses(userSelection),
  media: getSelectedMedia(userSelection),
  meterStatuses: getSelectedMeterStatuses(userSelection),
  secondaryAddresses: getSelectedSecondaryAddresses(userSelection),
  selectionId: userSelection.userSelection.id,
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  toggleParameter,
}, dispatch);

export const SelectionContentContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(SelectionContent);

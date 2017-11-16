import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import 'SelectionContentContainer.scss';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {toggleSelection} from '../../../state/search/selection/selectionActions';
import {
  LookupState,
  parameterNames,
  SelectionListItem,
  SelectionParameter,
} from '../../../state/search/selection/selectionModels';
import {
  getAddresses,
  getAlarms,
  getCities,
  getCitiesSelection, getManufacturers,
} from '../../../state/search/selection/selectionSelectors';
import {IdNamed} from '../../../types/Types';
import {MultiDropdownSelector} from '../../common/components/dropdown-selector/MultiDropdownSelector';
import {SimpleDropdownSelector} from '../../common/components/dropdown-selector/SimpleDropdownSelector';
import {Column} from '../../common/components/layouts/column/Column';
import {Row} from '../../common/components/layouts/row/Row';
import {Subtitle} from '../../common/components/texts/Titles';
import {MetersResultContainer} from './MetersContainer';

interface StateToProps {
  cities: SelectionListItem[];
  addresses: SelectionListItem[];
  alarms: SelectionListItem[];
  manufacturers: SelectionListItem[];
  citiesSelection: DomainModel<IdNamed>;
}

interface DispatchToProps {
  toggleSelection: (searchParameters: SelectionParameter) => void;
}

const SelectionContent = (props: StateToProps & DispatchToProps) => {
  const {toggleSelection, cities, addresses, alarms, manufacturers, citiesSelection} = props;

  const selectCity = (selection: IdNamed) => toggleSelection({...selection, parameter: parameterNames.cities});
  const selectAddress = (selection: IdNamed) => toggleSelection({...selection, parameter: parameterNames.addresses});
  const selectAlarm = (selection: IdNamed) => toggleSelection({...selection, parameter: parameterNames.alarms});
  const selectManufacturer = (selection: IdNamed) =>
    toggleSelection({...selection, parameter: parameterNames.manufacturers});

  const citySelectionText = translate('city') + ': ';
  const addressSelectionText = translate('address') + ': ';
  const alarmSelectionText = translate('alarm') + ': ';
  const manufacturerSelectionText = translate('manufacturer') + ': ';

  return (
    <Column className="SelectionContentBox">
      <Subtitle>{translate('filter')}</Subtitle>

      <Row className="SelectionDropdownOptions">
        <SimpleDropdownSelector
          list={cities}
          selectionText={citySelectionText}
          select={selectCity}
        />
        <MultiDropdownSelector
          list={addresses}
          selectionText={addressSelectionText}
          select={selectAddress}
          parentSelectionLookup={citiesSelection}
          parentIdentifier="cityId"
        />
        <SimpleDropdownSelector
          list={alarms}
          selectionText={alarmSelectionText}
          select={selectAlarm}
        />
        <SimpleDropdownSelector
          list={manufacturers}
          selectionText={manufacturerSelectionText}
          select={selectManufacturer}
        />
      </Row>

      <MetersResultContainer/>
    </Column>
  );
};

const mapStateToProps = ({searchParameters: {selection}, domainModels}: RootState): StateToProps => {
  const lookupState: LookupState = {
    domainModels,
    selection,
  };

  return {
    cities: getCities(lookupState),
    citiesSelection: getCitiesSelection(lookupState).entities,
    addresses: getAddresses(lookupState),
    alarms: getAlarms(lookupState),
    manufacturers: getManufacturers(lookupState),
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  toggleSelection,
}, dispatch);

export const SelectionContentContainer =
  connect<StateToProps, DispatchToProps, {}>(mapStateToProps, mapDispatchToProps)(SelectionContent);

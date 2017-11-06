import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {toggleSelection} from '../../../state/search/selection/selectionActions';
import {LookupState, parameterNames, SelectionParameter} from '../../../state/search/selection/selectionModels';
import {getListAddresses, getListCities, SelectionListItem} from '../../../state/search/selection/selectionSelectors';
import {IdNamed} from '../../../types/Types';
import {DropdownSelector} from '../../common/components/dropdown-selector/DropdownSelector';
import {Column} from '../../common/components/layouts/column/Column';
import {Row} from '../../common/components/layouts/row/Row';
import {MetersResultContainer} from '../containers/MetersContainer';

interface StateToProps {
  cities: SelectionListItem[];
  addresses: SelectionListItem[];
}

interface DispatchToProps {
  toggleSelection: (searchParameters: SelectionParameter) => void;
}

const SelectionContentBox = (props: StateToProps & DispatchToProps) => {
  const {
    toggleSelection,
    cities,
    addresses,
  } = props;

  const selectCity = (selection: IdNamed) => toggleSelection({...selection, parameter: parameterNames.cities});
  const selectAddress = (selection: IdNamed) => toggleSelection({...selection, parameter: parameterNames.addresses});

  const citySelectionText = translate('city') + ': ';
  const addressSelectionText = translate('address') + ': ';

  return (
    <Column className="SelectionContentBox">
      <Row>
        <DropdownSelector
          list={cities}
          selectionText={citySelectionText}
          onClick={selectCity}
        />
        <DropdownSelector
          list={addresses}
          selectionText={addressSelectionText}
          onClick={selectAddress}
        />
      </Row>

      <MetersResultContainer/>
    </Column>
  );
};

const mapStateToProps = ({searchParameters: {selection}, domainModels: {geoData}}: RootState): StateToProps => {
  const lookupState: LookupState = {
    geoData,
    selection,
  };

  return {
    cities: getListCities(lookupState),
    addresses: getListAddresses(lookupState),
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  toggleSelection,
}, dispatch);

export const SelectionContentBoxContainer =
  connect<StateToProps, DispatchToProps, {}>(mapStateToProps, mapDispatchToProps)(SelectionContentBox);

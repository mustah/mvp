import * as React from 'react';
import 'SelectionContentBox.scss';
import {IdNamed} from '../../../types/Types';
import {DropdownSelector} from '../../common/components/dropdown-selector/DropdownSelector';
import {Column} from '../../common/components/layouts/column/Column';
import {Row} from '../../common/components/layouts/row/Row';
import {SelectionDispatchToProps, SelectionStateToProps} from '../containers/SelectionContainer';
import {
  getDeselectedAddresses,
  getDeselectedCities,
  getSelectedAddresses,
  getSelectedCities,
} from '../../../state/search/selection/selectionSelectors';
import {SearchResultList} from './SelectionResultList';

export const SelectionContentBox = (props: SelectionStateToProps & SelectionDispatchToProps) => {
  const {toggleSearchOption, selection} = props;
  const selectCity = (selection: IdNamed) => toggleSearchOption({...selection, entity: 'cities'});
  const selectAddress = (selection: IdNamed) => toggleSearchOption({...selection, entity: 'addresses'});

  return (
    <Column className="SelectionContentBox">
      <Row>
        <DropdownSelector
          selectedList={getSelectedCities(selection)}
          list={getDeselectedCities(selection)}
          selectionText="Stad: "
          onClick={selectCity}
        />
        <DropdownSelector
          selectedList={getSelectedAddresses(selection)}
          list={getDeselectedAddresses(selection)}
          selectionText="Address: "
          onClick={selectAddress}
        />
      </Row>

      <SearchResultList/>
    </Column>
  );
};

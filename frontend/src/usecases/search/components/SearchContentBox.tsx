import * as React from 'react';
import 'SearchContentBox.scss';
import {DropdownSelector} from '../../common/components/dropdown-selector/DropdownSelector';
import {IdNamed} from '../../../types/Types';
import {Column} from '../../common/components/layouts/column/Column';
import {Row} from '../../common/components/layouts/row/Row';
import {SearchDispatchToProps, SearchStateToProps} from '../containers/SearchContainer';
import {getDeselectedAddresses, getDeselectedCities, getSelectedAddresses, getSelectedCities} from '../searchSelectors';
import {SearchResultList} from './SearchResultList';

export const SearchContentBox = (props: SearchStateToProps & SearchDispatchToProps) => {
  const {toggleSearchOption, search} = props;
  const selectCity = (selection: IdNamed) => toggleSearchOption({...selection, entity: 'cities'});
  const selectAddress = (selection: IdNamed) => toggleSearchOption({...selection, entity: 'addresses'});

  return (
    <Column className="SearchContentBox">
      <Row>
        <DropdownSelector
          selectedList={getSelectedCities(search)}
          list={getDeselectedCities(search)}
          selectionText="Stad: Alla"
          onClick={selectCity}
        />
        <DropdownSelector
          selectedList={getSelectedAddresses(search)}
          list={getDeselectedAddresses(search)}
          selectionText="Address: Alla"
          onClick={selectAddress}
        />
      </Row>

      <SearchResultList/>
    </Column>
  );
};

import * as React from 'react';
import 'SearchContentBox.scss';
import {IdNamed} from '../../../types/Types';
import {Column} from '../../common/components/layouts/column/Column';
import {Row} from '../../common/components/layouts/row/Row';
import {SearchDispatchToProps, SearchStateToProps} from '../containers/SearchContainer';
import {getDeselectedAddresses, getDeselectedCities, getSelectedAddresses, getSelectedCities} from '../searchSelectors';
import {DropDownSelector} from './DropDownSelector';
import {SearchResultList} from './SearchResultList';

export const SearchContentBox = (props: SearchStateToProps & SearchDispatchToProps) => {
  const {selectSearchOption, search} = props;
  const selectCity = (selection: IdNamed) => selectSearchOption({...selection, entity: 'cities'});
  const selectAddress = (selection: IdNamed) => selectSearchOption({...selection, entity: 'addresses'});

  return (
    <Column className="SearchContentBox">
      <Row>
        <DropDownSelector
          selectedList={getSelectedCities(search)}
          list={getDeselectedCities(search)}
          selectionText="Stad: Alla"
          onClick={selectCity}
        />
        <DropDownSelector
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

import * as React from 'react';
import {translate} from '../../../services/translationService';
import {parameterNames, SelectionParameter} from '../../../state/search/selection/selectionModels';
import {SelectionState} from '../../../state/search/selection/selectionReducer';
import {
  getDeselectedAddresses,
  getDeselectedCities,
  getSelectedAddresses,
  getSelectedCities,
} from '../../../state/search/selection/selectionSelectors';
import {PaginationProps} from '../../../state/ui/pagination/paginationModels';
import {IdNamed} from '../../../types/Types';
import {DropdownSelector} from '../../common/components/dropdown-selector/DropdownSelector';
import {Column} from '../../common/components/layouts/column/Column';
import {Row} from '../../common/components/layouts/row/Row';
import {PaginationControl} from '../../common/components/pagination-control/PaginationControl';
import {NormalizedRows} from '../../common/components/table/table/Table';
import {SearchResultList} from './SelectionResultList';

interface SelectionContentBoxProps {
  selection: SelectionState;
  toggleSelection: (searchParameters: SelectionParameter) => void;
  data: NormalizedRows;
  paginationProps: PaginationProps;
}

export const SelectionContentBox = (props: SelectionContentBoxProps) => {
  const {toggleSelection, selection, data, paginationProps} = props;
  const selectCity = (selection: IdNamed) => toggleSelection({...selection, parameter: parameterNames.cities});
  const selectAddress = (selection: IdNamed) => toggleSelection({...selection, parameter: parameterNames.addresses});

  return (
    <Column className="SelectionContentBox">
      <Row>
        <DropdownSelector
          selectedList={getSelectedCities(selection)}
          list={getDeselectedCities(selection)}
          selectionText={translate('city') + ': '}
          onClick={selectCity}
        />
        <DropdownSelector
          selectedList={getSelectedAddresses(selection)}
          list={getDeselectedAddresses(selection)}
          selectionText={translate('address') + ': '}
          onClick={selectAddress}
        />
      </Row>

      <SearchResultList data={data}/>
      <PaginationControl {...paginationProps}/>
    </Column>
  );
};

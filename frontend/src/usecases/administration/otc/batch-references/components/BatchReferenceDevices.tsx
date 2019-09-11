import * as React from 'react';
import {colors} from '../../../../../app/colors';
import {ListingDropdownSelector} from '../../../../../components/dropdown-selector/DropdownSelector';
import {noop} from '../../../../../helpers/commonHelpers';
import {firstUpperTranslated} from '../../../../../services/translationService';
import {fetchBatchDevices} from '../../../../../state/domain-models-paginated/batch-references/batchReferenceApiActions';

const style: React.CSSProperties = {
  backgroundColor: colors.dividerColor,
  marginBottom: 0,
  marginRight: 0,
};

const overviewText = (_, __) => '';

interface Props {
  id: string;
}

export const BatchReferenceDevices = ({id}: Props) => {
  const fetchItems = fetchBatchDevices(id);
  return (
    <ListingDropdownSelector
      innerDivClassName="DropdownSelector-Grid"
      innerDivStyle={style}
      fetchItems={fetchItems}
      isReadOnlyList={true}
      overviewText={overviewText}
      selectedItems={[]}
      selectionText={firstUpperTranslated('show devices')}
      select={noop}
      shouldFetchItemsOnMount={false}
    />
  );
};

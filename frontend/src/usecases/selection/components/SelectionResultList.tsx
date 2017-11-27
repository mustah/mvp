import * as React from 'react';
import 'SelectionResultList.scss';
import {Column} from '../../../components/layouts/column/Column';
import {MeterList} from '../../../components/metering-point/MeterList';
import {TableProps} from '../../../components/tabs/models/TabsModel';
import {OnClickWithId} from '../../../types/Types';
import {Meter} from '../../../state/domain-models/meter/meterModels';

interface Props {
  selectEntryAdd: OnClickWithId;
}

export const SearchResultList = (props: TableProps<Meter> & Props) => {
  const {selectEntryAdd} = props;
  return (
    <Column className="SearchResultList">
      <MeterList {...props} selectEntryAdd={selectEntryAdd}/>
    </Column>
  );
};

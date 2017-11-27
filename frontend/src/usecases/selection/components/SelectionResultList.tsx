import * as React from 'react';
import 'SelectionResultList.scss';
import {Column} from '../../../components/layouts/column/Column';
import {MeterList} from '../../../components/metering-point/MeterList';
import {ListProps} from '../../../components/tabs/models/TabsModel';
import {OnClickWithId} from '../../../types/Types';

interface Props {
  selectEntryAdd: OnClickWithId;
}

export const SearchResultList = (props: ListProps & Props) => {
  const {data, selectEntryAdd} = props;
  return (
    <Column className="SearchResultList">
      <MeterList data={data} selectEntryAdd={selectEntryAdd}/>
    </Column>
  );
};

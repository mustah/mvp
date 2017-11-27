import * as React from 'react';
import 'SelectionResultList.scss';
import {Column} from '../../../components/layouts/column/Column';
import {MeterList} from '../../../components/metering-point/MeterList';
import {ListProps} from '../../../components/tabs/models/TabsModel';

export const SearchResultList = (props: ListProps) => {
  const {data} = props;
  return (
    <Column className="SearchResultList">
      <MeterList data={data}/>
    </Column>
  );
};

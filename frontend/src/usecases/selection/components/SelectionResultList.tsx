import * as React from 'react';
import 'SelectionResultList.scss';
import {Column} from '../../common/components/layouts/column/Column';
import {MeterList} from '../../common/components/metering-point/MeterList';
import {ListProps} from '../../common/components/tabs/models/TabsModel';

export const SearchResultList = (props: ListProps) => {
  const {data} = props;
  return (
    <Column className="SearchResultList">
      <MeterList data={data}/>
    </Column>
  );
};

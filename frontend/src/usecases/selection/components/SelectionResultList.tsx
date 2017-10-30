import * as React from 'react';
import 'SelectionResultList.scss';
import {Column} from '../../common/components/layouts/column/Column';
import {Row} from '../../common/components/layouts/row/Row';
import {Summary} from '../../common/components/summary/Summary';
import {MeterList} from '../../common/components/table/MeterList';
import {ListProps} from '../../common/components/tabs/models/TabsModel';

export const SearchResultList = (props: ListProps) => {
  const {data} = props;
  return (
    <Column className="SearchResultList">
    <Row className="SearchResultList-Summary">
      <Row className="Summaries">
        <Summary title="Städer" count="1"/>
        <Summary title="Adresser" count="2"/>
        <Summary title="Mätpunkter" count="4"/>
      </Row>
    </Row>
    <MeterList data={data}/>
  </Column>
  );
};

import * as React from 'react';
import 'TabOptions.scss';
import {Column} from '../../../layouts/components/column/Column';
import {Row} from '../../../layouts/components/row/Row';
import {TabUnderline} from '../tabUnderline/TabUnderliner';

export const TabOptions = (props) => {
  const {headers} = props;
  return (
    <Column className={'flex-1'}>
      <Row className={'Row-center'}>
        {headers}
      </Row>
      <TabUnderline/>
    </Column>
  );
};

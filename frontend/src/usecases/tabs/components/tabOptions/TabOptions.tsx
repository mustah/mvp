import * as React from 'react';
import {Icon} from '../../../common/components/icons/Icons';
import {Column} from '../../../layouts/components/column/Column';
import {Row} from '../../../layouts/components/row/Row';
import {TabUnderline} from '../tabUnderline/TabUnderliner';

export const TabOptions = (props) => {
  return (
    <Column>
      <Row className={'flex-1 Row-right'}>
        <Icon name={'magnify'} size="medium"/>
        <Icon name={'dots-vertical'} size="medium"/>
      </Row>
      <TabUnderline/>
    </Column>
  );
};

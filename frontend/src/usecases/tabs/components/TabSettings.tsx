import * as React from 'react';
import {Icon} from '../../common/components/icons/Icons';
import {Column} from '../../common/components/layouts/column/Column';
import {Row} from '../../common/components/layouts/row/Row';
import {TabUnderline} from './TabUnderliner';

export const TabSettings = (props) => {
  return (
    <Column>
      <Row className="flex-1 Row-right clickable">
        <Icon name="magnify" size="medium"/>
        <Icon name="dots-vertical" size="medium"/>
      </Row>
      <TabUnderline/>
    </Column>
  );
};

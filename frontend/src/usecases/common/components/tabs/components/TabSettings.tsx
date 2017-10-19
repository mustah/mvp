import * as React from 'react';
import {Icon} from '../../icons/Icon';
import {Column} from '../../layouts/column/Column';
import {Row} from '../../layouts/row/Row';
import {TabUnderline} from './TabUnderliner';

export interface TabSettingsProps {
  useCase: string;
}

export const TabSettings = (props: TabSettingsProps) => {
  return (
    <Column className="TabSettings">
      <Row className="flex-1 Row-right clickable">
        <Icon name="filter-outline" size="medium"/>
        <Icon name="dots-vertical" size="medium"/>
      </Row>
      <TabUnderline/>
    </Column>
  );
};

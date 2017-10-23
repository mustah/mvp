import * as React from 'react';
import {IconFilter} from '../../icons/IconFilter';
import {IconMore} from '../../icons/IconMore';
import {Column} from '../../layouts/column/Column';
import {Row} from '../../layouts/row/Row';
import {TabUnderline} from './TabUnderliner';

export interface TabSettingsProps {
  useCase: string;
}

export const TabSettings = (props: TabSettingsProps) => {
  const filter = () => console.log('filter'); // tslint:disable-line
  const more = () => console.log('more'); // tslint:disable-line
  return (
    <Column className="TabSettings">
      <Row className="flex-1 Row-right clickable">
        <IconFilter onClick={filter}/>
        <IconMore onClick={more}/>
      </Row>
      <TabUnderline/>
    </Column>
  );
};

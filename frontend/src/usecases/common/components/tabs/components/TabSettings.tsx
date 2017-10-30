import * as React from 'react';
import {IconMore} from '../../icons/IconMore';
import {Column} from '../../layouts/column/Column';
import {Row} from '../../layouts/row/Row';
import {TabUnderline} from './TabUnderliner';

export interface TabSettingsProps {
  useCase: string;
}

export const TabSettings = (props: TabSettingsProps) => {
  const more = () => console.log('more'); // tslint:disable-line
  // TODO Calle: open/close dropdown here, with the option "Export to Excel" to begin with
  return (
    <Column className="TabSettings">
      <Row className="flex-1 Row-right clickable">
        <IconMore onClick={more}/>
      </Row>
      <TabUnderline/>
    </Column>
  );
};

import * as React from 'react';
import {Column} from '../../common/components/layouts/column/Column';
import {Row} from '../../common/components/layouts/row/Row';
import {TabUnderline} from './TabUnderliner';

interface TabOptionsProps {
  children: any;
  forTab: string;
  selectedTab: string;
}

export const TabOptions = (props: TabOptionsProps) => {
  const {children, forTab, selectedTab} = props;
  if (forTab === selectedTab) {
    return (
      <Column className={'flex-1'}>
        <Row className={'Row-center'}>
          {children}
        </Row>
        <TabUnderline/>
      </Column>
    );
  } else {
    return null;
  }
};

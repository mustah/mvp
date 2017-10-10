import * as React from 'react';
import {Column} from '../../common/components/layouts/column/Column';
import {Row} from '../../common/components/layouts/row/Row';
import {tabType} from '../models/TabsModel';
import {TabOptionProps} from './TabOption';
import {TabUnderline} from './TabUnderliner';

export interface TabOptionsProps {
  children: Array<React.ReactElement<TabOptionProps>>;
  forTab: tabType;
  selectedTab: tabType;
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

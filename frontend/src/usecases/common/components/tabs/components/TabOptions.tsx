import * as React from 'react';
import {Column} from '../../layouts/column/Column';
import {Row} from '../../layouts/row/Row';
import {TabModel, tabType} from '../models/TabsModel';
import {TabOptionProps} from './TabOption';
import {TabUnderline} from './TabUnderliner';

export type SelectTab = (tab: tabType, option: string) => void;

export interface TabOptionsProps {
  children: Array<React.ReactElement<TabOptionProps>>;
  tab: tabType;
  selectedTab: tabType;
  select: SelectTab;
  tabs: TabModel;
}

export const TabOptions = (props: TabOptionsProps) => {
  const {children, tab, selectedTab, select, tabs} = props;
  const selectedOption = tabs[tab].selectedOption;
  const passDownProps = (child, index) => React.cloneElement(child, {tab, select, selectedOption, key: index});
  if (tab === selectedTab) {
    return (
      <Column className={'flex-1'}>
        <Row className={'Row-center'}>
          {children.map(passDownProps)}
        </Row>
        <TabUnderline/>
      </Column>
    );
  } else {
    return null;
  }
};

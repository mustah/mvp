import * as React from 'react';
import {Column} from '../../layouts/column/Column';
import {RowCenter} from '../../layouts/row/Row';
import {TabModel, TabName} from '../../../state/ui/tabs/tabsModels';
import {TabOptionProps} from './TabOption';
import {TabUnderline} from './TabUnderliner';

export type SelectTab = (tab: TabName, option: string) => void;

export interface TabOptionsProps {
  children: Array<React.ReactElement<TabOptionProps>>;
  tab: TabName;
  selectedTab: TabName;
  select: SelectTab;
  tabs: TabModel;
}

export const TabOptions = (props: TabOptionsProps) => {
  const {children, tab, selectedTab, select, tabs} = props;
  const selectedOption = tabs[tab].selectedOption;
  const passDownProps = (child, index) => React.cloneElement(child, {tab, select, selectedOption, key: index});
  const renderedChildren = children.map(passDownProps);
  if (tab === selectedTab) {
    return (
      <Column className={'flex-1'}>
        <RowCenter>
          {renderedChildren}
        </RowCenter>
        <TabUnderline/>
      </Column>
    );
  } else {
    return null;
  }
};

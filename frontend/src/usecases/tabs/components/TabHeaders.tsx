import * as React from 'react';
import {Row} from '../../layouts/components/row/Row';
import {tabType} from '../models/TabsModel';
import {TabProps} from './Tab';

export interface TabHeadersProps {
  children: Array<React.ReactElement<TabProps>>;
  selectedTab: tabType;
  onChangeTab: (tab: string) => void;
}

export const TabHeaders = (props: TabHeadersProps) => {
  const {children, selectedTab, onChangeTab} = props;
  const addPropsToChild = (child, index) => React.cloneElement(child, {selectedTab, onChangeTab, key: index});
  return (
    <Row className="TabHeaders">
      {children.map(addPropsToChild)}
    </Row>
  );
};

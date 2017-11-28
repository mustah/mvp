import * as React from 'react';
import {TopLevelTab} from '../models/TabsModel';
import {TabProps} from './Tab';
import {Row} from '../../layouts/row/Row';

export interface TabHeadersProps {
  children: Array<React.ReactElement<TabProps>> | React.ReactElement<TabProps>;
  selectedTab: TopLevelTab;
  onChangeTab: (tab: string) => void;
}

export const TabHeaders = (props: TabHeadersProps) => {
  const {children, selectedTab, onChangeTab} = props;
  const passDownProps = (child, index) => React.cloneElement(child, {selectedTab, onChangeTab, key: index});
  return (
    <Row className="TabHeaders">
      {Array.isArray(children) ? children.map(passDownProps) : passDownProps(children, 1)}
    </Row>
  );
};

import * as React from 'react';
import {tabType} from '../models/TabsModel';
import {TabProps} from './Tab';
import {Row} from '../../common/components/layouts/row/Row';

export interface TabHeadersProps {
  children: Array<React.ReactElement<TabProps>>;
  selectedTab: tabType;
  onChangeTab: (tab: string) => void;
}

export const TabHeaders = (props: TabHeadersProps) => {
  const {children, selectedTab, onChangeTab} = props;
  const passDownProps = (child, index) => React.cloneElement(child, {selectedTab, onChangeTab, key: index});
  return (
    <Row className="TabHeaders">
      {children.map(passDownProps)}
    </Row>
  );
};

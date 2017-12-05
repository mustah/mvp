import * as React from 'react';
import {TabName} from '../../../state/ui/tabs/tabsModels';
import {TabProps} from './Tab';
import {Row} from '../../layouts/row/Row';

export interface TabHeadersProps {
  children: Array<React.ReactElement<TabProps>> | React.ReactElement<TabProps>;
  selectedTab: TabName;
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

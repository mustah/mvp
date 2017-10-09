import * as React from 'react';
import {Row} from '../../layouts/components/row/Row';
import {TabProps} from './Tab';
import {TabOptionsProps} from './TabOptions';
import {TabSettingsProps} from './TabSettings';

type TabListChild = TabProps | TabOptionsProps | TabSettingsProps;

export interface TabListProps {
  children: Array<React.ReactElement<TabListChild>>;
}

export const TabList = (props: TabListProps) => {
  return (
    <Row className="TabList">
      {props.children}
    </Row>
  );
};

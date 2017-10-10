import * as React from 'react';
import {Row} from '../../layouts/components/row/Row';
import {TabHeadersProps} from './TabHeaders';
import {TabOptionsProps} from './TabOptions';
import {TabSettingsProps} from './TabSettings';

type TabTopBarChild = TabHeadersProps | TabOptionsProps | TabSettingsProps;

export interface TabTopBarProps {
  children: Array<React.ReactElement<TabTopBarChild>>;
}

export const TabTopBar = (props: TabTopBarProps) => {
  return (
    <Row className="TabList">
      {props.children}
    </Row>
  );
};

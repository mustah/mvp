import * as React from 'react';
import {Row} from '../../layouts/row/Row';
import {TabHeadersProps} from './TabHeaders';
import {TabOptionsProps} from './TabOptions';

type TabTopBarChild = TabHeadersProps | TabOptionsProps;

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

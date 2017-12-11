import * as React from 'react';
import {Row} from '../../layouts/row/Row';
import {TabHeadersProps} from './TabHeaders';

export interface TabTopBarProps {
  children: Array<React.ReactElement<TabHeadersProps>>;
}

export const TabTopBar = (props: TabTopBarProps) => {
  return (
    <Row className="TabList">
      {props.children}
    </Row>
  );
};

import * as React from 'react';
import {ItemOrArray} from '../../../types/Types';
import {Row} from '../../layouts/row/Row';
import {TabHeadersProps} from './TabHeaders';

export interface TabTopBarProps {
  children: ItemOrArray<React.ReactElement<TabHeadersProps>>;
}

export const TabTopBar = ({children}: TabTopBarProps) => (
  <Row className="TabList">
    {children}
  </Row>
);

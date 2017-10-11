import * as React from 'react';
import {TabHeadersProps} from './TabHeaders';
import {TabOptionsProps} from './TabOptions';
import {TabSettingsProps} from './TabSettings';
import {Row} from '../../common/components/layouts/row/Row';

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

import * as React from 'react';
import {Row} from '../../layouts/components/row/Row';

interface TabListProps {
  children: any; // TODO: Make type more specific.
}

export const TabList = (props: TabListProps) => {
  return (
    <Row className="TabList">
      {props.children}
    </Row>
  );
};

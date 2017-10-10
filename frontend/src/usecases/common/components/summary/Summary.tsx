import * as classNames from 'classnames';
import * as React from 'react';
import {Column} from '../layouts/column/Column';
import {Row} from '../layouts/row/Row';
import {Bold, Normal} from '../texts/Texts';
import './Summary.scss';

export interface SummaryProps {
  count: string;
  title: string;
}

export const Summary = (props: SummaryProps) => {
  const {count, title} = props;
  return (
    <div>
      <Column className="Summary">
        <Row className={classNames('Row-center Summary-name')}>
          <Normal>{title}</Normal>
        </Row>
        <Row className={classNames('Row-center Summary-count')}>
          <Bold>{count}</Bold>
        </Row>
      </Column>
    </div>
  );
};

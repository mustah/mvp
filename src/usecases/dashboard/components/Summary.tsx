import * as classNames from 'classnames';
import * as React from 'react';
import {Bold, Normal} from '../../common/components/texts/Texts';
import {Column} from '../../layouts/components/column/Column';
import {Row} from '../../layouts/components/row/Row';
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
          <Normal> {title}</Normal>
        </Row>
        <Row className={classNames('Row-center Summary-count')}>
          <Bold> {count}</Bold>
        </Row>
      </Column>
    </div>
  );
};

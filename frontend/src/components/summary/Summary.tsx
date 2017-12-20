import * as React from 'react';
import {Column} from '../layouts/column/Column';
import {RowCenter} from '../layouts/row/Row';
import {Bold, Small} from '../texts/Texts';
import './Summary.scss';

export interface SummaryProps {
  count: number;
  title: string;
}

export const Summary = (props: SummaryProps) => {
  const {count, title} = props;
  return (
    <Column className="Summary">
      <RowCenter className="Summary-title">
        <Small className="uppercase">{title}</Small>
      </RowCenter>
      <RowCenter>
        <Bold className="Summary-value">{count}</Bold>
      </RowCenter>
    </Column>
  );
};
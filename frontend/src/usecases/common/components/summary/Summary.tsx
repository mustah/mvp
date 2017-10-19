import * as React from 'react';
import {Column} from '../layouts/column/Column';
import {RowCenter} from '../layouts/row/Row';
import {Bold, Small} from '../texts/Texts';
import './Summary.scss';

export interface SummaryProps {
  count: string;
  title: string;
}

export const Summary = (props: SummaryProps) => {
  const {count, title} = props;
  return (
    <Column className="Summary">
      <RowCenter>
        <Small className="uppercase">{title}</Small>
      </RowCenter>
      <RowCenter>
        <Bold>{count}</Bold>
      </RowCenter>
    </Column>
  );
};

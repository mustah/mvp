import * as classNames from 'classnames';
import * as React from 'react';
import {Bold, Normal} from '../../../common/components/texts/Texts';
import {Column} from '../../../layouts/components/column/Column';
import {Row} from '../../../layouts/components/row/Row';
import './StatusBox.scss';

export interface StatusBoxProps {
  color?: string;
  count: string;
  title: string;
  value: string;
}

export const StatusBox = (props: StatusBoxProps) => {
  const {count, color, title, value} = props;
  return (
      <Column className={classNames('StatusBox Column-center', color)}>
        <Row className={classNames('Row-center StatusBox-name')}>
          <Normal>{title}</Normal>
        </Row>
        <Row className={classNames('Row-center StatusBox-value')}>
          <Bold>{value}</Bold>
        </Row>
        <Row className={classNames('Row-center StatusBox-count')}>
          <Bold>{count}</Bold>
        </Row>
      </Column>
  );
};
